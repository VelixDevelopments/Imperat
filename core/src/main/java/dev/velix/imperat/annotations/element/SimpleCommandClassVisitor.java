package dev.velix.imperat.annotations.element;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.AnnotationHelper;
import dev.velix.imperat.annotations.AnnotationReader;
import dev.velix.imperat.annotations.MethodCommandExecutor;
import dev.velix.imperat.annotations.parameters.AnnotationParameterDecorator;
import dev.velix.imperat.annotations.parameters.NumericParameterDecorator;
import dev.velix.imperat.annotations.types.Optional;
import dev.velix.imperat.annotations.types.*;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.NumericRange;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.help.MethodHelpExecution;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.Pair;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;

final class SimpleCommandClassVisitor<S extends Source> extends CommandClassVisitor<S> {
    
    
    SimpleCommandClassVisitor(Imperat<S> imperat) {
        super(imperat);
    }

    
    @Override
    public Set<Command<S>> visitCommandClass(
            @NotNull AnnotationReader<S> reader,
            @NotNull ClassElement clazz
    ) {
        
        Set<Command<S>> commands = new HashSet<>();
        
        Annotation commandAnnotation = getCommandAnnotation(clazz);
        
        if (clazz.isRootClass() && commandAnnotation != null) {
            if (clazz.isAnnotationPresent(SubCommand.class)) {
                throw new IllegalStateException("Root command class cannot be a @SubCommand");
            }
            Command<S> cmd = loadCommand(reader, null, clazz, commandAnnotation);
            
            //if cmd=null → loading @Command methods only from this class
            if (cmd != null) {
                loadCommandClassForRootClass(reader, clazz, cmd);
                commands.add(cmd);
            }
            
        } else if (commandAnnotation != null) {
            //inner class with @Command or @SubCommand
            Command<S> cmd = loadCommand(reader, null, clazz, commandAnnotation);
            assert cmd != null;
            loadCommandClassForRootClass(reader, clazz, cmd);
            commands.add(cmd);
        } else {
            //no annotation
            for (ParseElement<?> element : clazz.getChildren()) {
                if (element.isAnnotationPresent(dev.velix.imperat.annotations.types.Command.class)) {
                    var cmd = loadCommand(reader, null, element, Objects.requireNonNull(element.getAnnotation(dev.velix.imperat.annotations.types.Command.class)));
                    if (cmd != null) {
                        imperat.registerCommand(cmd);
                    }
                }
            }
            
        }
        
        return commands;
    }
    
    
    private Annotation getCommandAnnotation(ClassElement clazz) {
        if (clazz.isAnnotationPresent(dev.velix.imperat.annotations.types.Command.class))
            return clazz.getAnnotation(dev.velix.imperat.annotations.types.Command.class);
        
        if (clazz.isAnnotationPresent(SubCommand.class))
            return clazz.getAnnotation(SubCommand.class);
        
        return null;
    }
    
    
    private void loadCommandClassForRootClass(AnnotationReader<S> reader, ClassElement clazz, @NotNull Command<S> cmd) {
        for (ParseElement<?> element : clazz.getChildren()) {
            if (element instanceof MethodElement method) {
                if (method.isAnnotationPresent(dev.velix.imperat.annotations.types.Command.class)) {
                    var cmdAnn = method.getAnnotation(dev.velix.imperat.annotations.types.Command.class);
                    assert cmdAnn != null;
                    imperat.registerCommand(loadCommand(reader, null, method, cmdAnn));
                } else if (method.isAnnotationPresent(Usage.class)) {
                    cmd.addUsage(loadUsage(reader, null, cmd, method));
                } else if (method.isAnnotationPresent(SubCommand.class)) {
                    SubCommand subAnn = method.getAnnotation(SubCommand.class);
                    assert subAnn != null;
                    cmd.addSubCommand(loadCommand(reader, cmd, method, subAnn), Boolean.TRUE.equals(subAnn.attachDirectly()));
                }
            } else if (element instanceof ClassElement innerClass) {
                
                SubCommand subCommand = innerClass.getAnnotation(SubCommand.class);
                if (subCommand != null) {
                    var sub = loadCommand(reader, cmd, innerClass, subCommand);
                    cmd.addSubCommand(sub, Boolean.TRUE.equals(subCommand.attachDirectly()));
                } else {
                    throw new IllegalArgumentException("Failed to load command elements from inner class" + innerClass.getElement().getSimpleName());
                }
                
            } else {
                throw new IllegalStateException();
            }
        }
    }
    
    private Command<S> loadCmdInstance(Annotation cmdAnnotation) {
        
        if (cmdAnnotation instanceof dev.velix.imperat.annotations.types.Command cmdAnn) {
            final String[] values = cmdAnn.value();
            assert values != null;
            
            List<String> aliases = new ArrayList<>(Arrays.asList(values)
                    .subList(1, values.length));
            
            boolean ignoreAC = cmdAnn.ignoreAutoCompletionPermission();
            
            return Command.<S>create(values[0])
                    .ignoreACPermissions(ignoreAC)
                    .aliases(aliases)
                    .build();
            
        } else if (cmdAnnotation instanceof SubCommand subCommand) {
            final String[] values = subCommand.value();
            assert values != null;
            
            List<String> aliases = new ArrayList<>(Arrays.asList(values)
                    .subList(1, values.length));
            
            boolean ignoreAC = subCommand.ignoreAutoCompletionChecks();
            
            return Command.<S>create(values[0])
                    .ignoreACPermissions(ignoreAC)
                    .aliases(aliases)
                    .build();
        }
        
        return null;
    }
    
    private Command<S> loadCommand(
            AnnotationReader<S> reader,
            @Nullable Command<S> parentCmd,
            ParseElement<?> parseElement,
            @NotNull Annotation annotation
    ) {
        
        Command<S> cmd = loadCmdInstance(annotation);
        if (parseElement instanceof MethodElement methodElement && cmd != null) {
            //@Command on method
            if (methodElement.getParameters().size() == 1) {
                //default usage for that command.
                cmd.setDefaultUsageExecution(
                        new MethodCommandExecutor<>(
                                reader.getRootClass(), imperat, methodElement.getElement(), Collections.emptyList()
                        )
                );
            } else {
                var usage = loadUsage(reader, parentCmd, cmd, methodElement);
                //CommandDebugger.debugParameters("sub usage params: " , usage.getParameters());
                cmd.addUsage(usage);
            }
            
            return cmd;
        } else if (parseElement instanceof ClassElement commandClass) {
            
            
            //load command class
            for (ParseElement<?> element : commandClass.getChildren()) {
                
                if (element instanceof MethodElement method) {
                    if (cmd == null) {
                        throw new IllegalStateException("Method  '" + method.getElement().getName() + "' Cannot be treated as usage/subcommand, it doesn't have a parent ");
                    }
                    
                    if (method.isAnnotationPresent(Usage.class)) {
                        if (method.getParameters().size() == 1) {
                            //default usage for that command.
                            cmd.setDefaultUsageExecution(
                                    new MethodCommandExecutor<>(
                                            reader.getRootClass(), imperat, method.getElement(), Collections.emptyList()
                                    )
                            );
                        } else {
                            cmd.addUsage(loadUsage(reader, parentCmd, cmd, method));
                        }
                    } else if (method.isAnnotationPresent(SubCommand.class)) {
                        var subAnn = method.getAnnotation(SubCommand.class);
                        assert subAnn != null;
                        var subCmd = loadCommand(reader, cmd, method, subAnn);
                        assert subCmd != null;
                        if (method.getParameters().size() == 1) {
                            //default usage for that command.
                            subCmd.setDefaultUsageExecution(
                                    new MethodCommandExecutor<>(
                                            reader.getRootClass(), imperat, method.getElement(), Collections.emptyList()
                                    )
                            );
                        } else {
                            subCmd.addUsage(loadUsage(reader, parentCmd, cmd, method));
                        }
                        cmd.addSubCommand(subCmd, subAnn.attachDirectly());
                    }
                } else if (element instanceof ClassElement innerClass) {
                    
                    if (innerClass.isAnnotationPresent(dev.velix.imperat.annotations.types.Command.class)) {
                        //separate embedded command
                        var innerCmdAnn = innerClass.getAnnotation(dev.velix.imperat.annotations.types.Command.class);
                        assert innerCmdAnn != null;
                        imperat.registerCommand(
                                loadCommand(reader, null, innerClass, innerCmdAnn)
                        );
                        return null;
                    } else if (innerClass.isAnnotationPresent(SubCommand.class)) {
                        if (cmd == null) {
                            throw new IllegalStateException("Inner class '" + innerClass.getElement().getSimpleName() + "' Cannot be  treated as subcommand, it doesn't have a parent ");
                        }
                        SubCommand subCommandAnn = innerClass.getAnnotation(SubCommand.class);
                        assert subCommandAnn != null;
                        cmd.addSubCommand(loadCommand(reader, cmd, innerClass, subCommandAnn), subCommandAnn.attachDirectly());
                    }
                    
                }
                
                
            }
            
        }
        
        return cmd;
    }
    
    
    private CommandUsage<S> loadUsage(AnnotationReader<S> reader, @Nullable Command<S> parentCmd,
                                      @NotNull Command<S> loadedCmd, MethodElement element) {
        Method method = element.getElement();
        if (method.getParameters().length == 1) {
            loadedCmd.setDefaultUsageExecution(new MethodCommandExecutor<>(reader.getRootClass(), imperat, method, Collections.emptyList()));
            return CommandUsage.<S>builder().build(loadedCmd);
        }
        
        ClassElement methodOwner = (ClassElement) element.getParent();
        assert methodOwner != null;
        
        var parametersInfo = loadParameters(element, loadedCmd, parentCmd);
        
        final boolean isHelp = parametersInfo.left();
        final List<CommandParameter> params = parametersInfo.right();
        
        var execution = isHelp ? new MethodHelpExecution<>(imperat, reader.getRootClass(), method, parametersInfo.right())
                : new MethodCommandExecutor<>(reader.getRootClass(), imperat, method, params);
        
        return CommandUsage.<S>builder().parameters(params)
                .execute(execution).build(loadedCmd, isHelp);
    }
    
    private Pair<List<CommandParameter>, Boolean> loadParameters(
            @NotNull MethodElement method,
            @NotNull Command<S> loadedCmd,
            @Nullable Command<S> parentCmd
    ) {
        
        List<CommandParameter> toLoad = new ArrayList<>();
        CommandUsage<S> main = parentCmd == null ? loadedCmd.getMainUsage() : parentCmd.getMainUsage();
        
        LinkedList<CommandParameter> mainUsageParameters = new LinkedList<>(main.getParameters());
        LinkedList<ParameterElement> parameterElements = new LinkedList<>(method.getParameters());
        
        boolean help = false;
        while (!parameterElements.isEmpty()) {
            
            ParameterElement parameterElement = parameterElements.peek();
            if (parameterElement == null) break;
            Type type = parameterElement.getElement().getParameterizedType();
            
            if (imperat.canBeSender(type) || imperat.hasContextResolver(type)) {
                parameterElements.removeFirst();
                continue;
            }
            if (TypeUtility.areRelatedTypes(type, CommandHelp.class)) {
                //CommandHelp parameter
                help = true;
                continue;
            }
            
            CommandParameter commandParameter = loadParameter(parameterElement);
            
            CommandParameter mainParameter = mainUsageParameters.peek();
            if (mainParameter == null) {
                toLoad.add(commandParameter);
                parameterElements.removeFirst();
                continue;
            }
            if (parentCmd != null && mainParameter.isSimilarTo(commandParameter)) {
                parameterElements.removeFirst();
                mainUsageParameters.removeFirst();
                continue;
            }
            
            toLoad.add(commandParameter);
            mainUsageParameters.removeFirst();
            parameterElements.removeFirst();
        }
        
        return new Pair<>(toLoad, help);
    }
    
    @SuppressWarnings("unchecked")
    private <T> CommandParameter loadParameter(
            @NotNull ParseElement<?> paramElement
    ) {
        
        ParameterElement element = (ParameterElement) paramElement;
        Parameter parameter = element.getElement();
        
        Named named = parameter.getAnnotation(Named.class);
        Flag flag = parameter.getAnnotation(Flag.class);
        Switch switchAnnotation = parameter.getAnnotation(Switch.class);
        
        if (flag != null && switchAnnotation != null) {
            throw new IllegalStateException("both @Flag and @Switch at the same time !");
        }
        
        String name = AnnotationHelper.getParamName(parameter, named, flag, switchAnnotation);
        boolean optional = flag != null || switchAnnotation != null
                || element.isAnnotationPresent(Optional.class);
        
        //reading suggestion annotation
        //element.debug();
        
        Suggest suggestAnnotation = element.getAnnotation(Suggest.class);
        SuggestionProvider suggestionProvider = element.getAnnotation(SuggestionProvider.class);
        
        SuggestionResolver<S, T> suggestionResolver = null;
        
        if (suggestAnnotation != null) {
            suggestionResolver = (SuggestionResolver<S, T>) SuggestionResolver.plain(parameter.getType(), suggestAnnotation.value());
        } else if (suggestionProvider != null) {
            suggestionResolver = imperat.getNamedSuggestionResolver(suggestionProvider.value().toLowerCase());
        }
        
        boolean greedy = parameter.getAnnotation(Greedy.class) != null;
        
        if (greedy && parameter.getType() != String.class) {
            throw new IllegalArgumentException("Argument '" + parameter.getName() + "' is greedy while having a non-greedy type '" + parameter.getType().getName() + "'");
        }
        
        dev.velix.imperat.command.Description desc = dev.velix.imperat.command.Description.EMPTY;
        if (element.isAnnotationPresent(Description.class)) {
            var descAnn = element.getAnnotation(Description.class);
            assert descAnn != null;
            desc = dev.velix.imperat.command.Description.of(descAnn.value());
        }
        
        String permission = null;
        if (element.isAnnotationPresent(Permission.class)) {
            var permAnn = element.getAnnotation(Permission.class);
            assert permAnn != null;
            permission = permAnn.value();
        }
        
        OptionalValueSupplier<T> optionalValueSupplier = null;
        if (optional) {
            DefaultValue defaultValueAnnotation = parameter.getAnnotation(DefaultValue.class);
            DefaultValueProvider provider = parameter.getAnnotation(DefaultValueProvider.class);
            optionalValueSupplier = AnnotationHelper.deduceOptionalValueSupplier(parameter, defaultValueAnnotation, provider);
        }
        
        if (flag != null) {
            String[] flagAliases = flag.value();
            return AnnotationParameterDecorator.decorate(
                    CommandParameter.<S, T>flag(name, (Class<T>) flag.inputType())
                            .aliases(getAllExceptFirst(flagAliases))
                            .flagDefaultInputValue(optionalValueSupplier)
                            .description(desc)
                            .permission(permission)
                            .build(),
                    element
            );
        } else if (switchAnnotation != null) {
            String[] switchAliases = switchAnnotation.value();
            return AnnotationParameterDecorator.decorate(
                    CommandParameter.<S>flagSwitch(name)
                            .aliases(getAllExceptFirst(switchAliases))
                            .description(desc)
                            .permission(permission)
                            .build(),
                    element
            );
        }
        
        CommandParameter param =
                AnnotationParameterDecorator.decorate(
                        CommandParameter.of(
                                name, TypeWrap.of((Class<T>) parameter.getType()), permission, desc,
                                optional, greedy, optionalValueSupplier, suggestionResolver
                        ), element
                );
        
        if (TypeUtility.isNumericType(TypeWrap.of(param.getType()))
                && element.isAnnotationPresent(Range.class)) {
            Range range = element.getAnnotation(Range.class);
            assert range != null;
            param = NumericParameterDecorator.decorate(
                    param, NumericRange.of(range.min(), range.max())
            );
        }
        
        return param;
    }
    
    private List<String> getAllExceptFirst(String[] array) {
        List<String> flagAliases = new ArrayList<>(array.length - 1);
        flagAliases.addAll(Arrays.asList(array).subList(1, array.length));
        return flagAliases;
    }
    
    
}
