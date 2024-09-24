package dev.velix.imperat.annotations.base.element;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.Optional;
import dev.velix.imperat.annotations.*;
import dev.velix.imperat.annotations.base.AnnotationHelper;
import dev.velix.imperat.annotations.base.AnnotationRegistry;
import dev.velix.imperat.annotations.base.MethodCommandExecutor;
import dev.velix.imperat.annotations.base.element.selector.ElementSelector;
import dev.velix.imperat.annotations.parameters.AnnotationParameterDecorator;
import dev.velix.imperat.annotations.parameters.NumericParameterDecorator;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandCoordinator;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.NumericRange;
import dev.velix.imperat.command.parameters.StrictParameterList;
import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.resolvers.TypeSuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.Pair;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import dev.velix.imperat.util.reflection.Reflections;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;

final class SimpleCommandClassVisitor<S extends Source> extends CommandClassVisitor<S> {
    
    
    SimpleCommandClassVisitor(Imperat<S> imperat, AnnotationRegistry registry, ElementSelector<MethodElement> methodSelector) {
        super(imperat, registry, methodSelector);
    }
    
    
    @Override
    public Set<Command<S>> visitCommandClass(
            @NotNull ClassElement clazz
    ) {
        
        Set<Command<S>> commands = new HashSet<>();
        
        Annotation commandAnnotation = getCommandAnnotation(clazz);
        if (clazz.isRootClass() && commandAnnotation != null && clazz.isAnnotationPresent(SubCommand.class)) {
            throw new IllegalStateException("Root command class cannot be a @SubCommand");
        }
        
        if (commandAnnotation != null) {
            
            Command<S> cmd = loadCommand(null, clazz, commandAnnotation);
            
            //if cmd=null → loading @Command methods only from this class
            if (cmd != null) {
                loadCommandMethods(clazz);
                commands.add(cmd);
            }
        } else {
            //no annotation
            for (ParseElement<?> element : clazz.getChildren()) {
                if (element.isAnnotationPresent(dev.velix.imperat.annotations.Command.class)) {
                    var cmd = loadCommand(null, element, Objects.requireNonNull(element.getAnnotation(dev.velix.imperat.annotations.Command.class)));
                    if (cmd != null) {
                        imperat.registerCommand(cmd);
                    }
                }
            }
            
        }
        
        return commands;
    }
    
    
    private Annotation getCommandAnnotation(ClassElement clazz) {
        if (clazz.isAnnotationPresent(dev.velix.imperat.annotations.Command.class))
            return clazz.getAnnotation(dev.velix.imperat.annotations.Command.class);
        
        if (clazz.isAnnotationPresent(SubCommand.class))
            return clazz.getAnnotation(SubCommand.class);
        
        return null;
    }
    
    
    private void loadCommandMethods(ClassElement clazz) {
        for (ParseElement<?> element : clazz.getChildren()) {
            if (element instanceof MethodElement method && method.isAnnotationPresent(dev.velix.imperat.annotations.Command.class)) {
                var cmdAnn = method.getAnnotation(dev.velix.imperat.annotations.Command.class);
                assert cmdAnn != null;
                imperat.registerCommand(loadCommand(null, method, cmdAnn));
            }
        }
    }
    
    private Command<S> loadCmdInstance(Annotation cmdAnnotation, ParseElement<?> element) {
        PreProcessor preProcessor = element.getAnnotation(PreProcessor.class);
        PostProcessor postProcessor = element.getAnnotation(PostProcessor.class);
        
        Permission permission = element.getAnnotation(Permission.class);
        Description description = element.getAnnotation(Description.class);
        
        if (cmdAnnotation instanceof dev.velix.imperat.annotations.Command cmdAnn) {
            
            
            final String[] values = cmdAnn.value();
            final List<String> aliases = List.of(values).subList(1, values.length);
            final boolean ignoreAC = cmdAnn.skipSuggestionsChecks();
            
            var builder = Command.<S>create(values[0])
                    .ignoreACPermissions(ignoreAC)
                    .aliases(aliases);
            if (permission != null) {
                builder.permission(permission.value());
            }
            
            if (description != null) {
                builder.description(description.value());
            }
            
            if (preProcessor != null) {
                builder.preProcessor(loadPreProcessorInstance(preProcessor.value()));
            }
            
            if (postProcessor != null) {
                builder.postProcessor(loadPostProcessorInstance(postProcessor.value()));
            }
            
            return builder.build();
            
        } else if (cmdAnnotation instanceof SubCommand subCommand) {
            final String[] values = subCommand.value();
            assert values != null;
            
            final List<String> aliases = List.of(values).subList(1, values.length);
            final boolean ignoreAC = subCommand.skipSuggestionsChecks();
            
            var builder = Command.<S>create(values[0])
                    .ignoreACPermissions(ignoreAC)
                    .aliases(aliases);
            
            if (permission != null) {
                builder.permission(permission.value());
            }
            
            if (description != null) {
                builder.description(description.value());
            }
            
            if (preProcessor != null) {
                builder.preProcessor(loadPreProcessorInstance(preProcessor.value()));
            }
            
            if (postProcessor != null) {
                builder.postProcessor(loadPostProcessorInstance(postProcessor.value()));
            }
            
            return builder.build();
        }
        return null;
    }
    
    private Command<S> loadCommand(
            @Nullable Command<S> parentCmd,
            ParseElement<?> parseElement,
            @NotNull Annotation annotation
    ) {
        final Command<S> cmd = loadCmdInstance(annotation, parseElement);
        if (parentCmd != null && cmd != null) {
            cmd.parent(parentCmd);
        }
        if (parseElement instanceof MethodElement method && cmd != null) {
            //@Command on method
            if (!methodSelector.canBeSelected(imperat, registry, method, true)) {
                return cmd;
            }
            if (method.getInputCount() == 0) {
                //default usage for that command.
                
                cmd.setDefaultUsageExecution(
                        MethodCommandExecutor.of(imperat, method, Collections.emptyList())
                );
                
            } else {
                var usage = loadUsage(parentCmd, cmd, method);
                //ImperatDebugger.debugParameters("sub usage params: " , usage.getParameters());
                
                if (usage != null) {
                    cmd.addUsage(usage);
                }
            }
            
            return cmd;
        } else if (parseElement instanceof ClassElement commandClass) {
            
            
            //load command class
            for (ParseElement<?> element : commandClass.getChildren()) {
                
                if (element instanceof MethodElement method) {
                    if (cmd == null) {
                        throw new IllegalStateException("Method  '" + method.getElement().getName() + "' Cannot be treated as usage/subcommand, it doesn't have a parent ");
                    }
                    
                    if (!methodSelector.canBeSelected(imperat, registry, method, true)) {
                        return cmd;
                    }
                    
                    if (method.isAnnotationPresent(Usage.class)) {
                        if (method.getInputCount() == 0) {
                            //default usage for that command.
                            cmd.setDefaultUsageExecution(
                                    MethodCommandExecutor.of(imperat, method, Collections.emptyList())
                            );
                            
                        } else {
                            var usage = loadUsage(parentCmd, cmd, method);
                            if (usage != null) {
                                cmd.addUsage(usage);
                            }
                        }
                    } else if (method.isAnnotationPresent(SubCommand.class)) {
                        var subAnn = method.getAnnotation(SubCommand.class);
                        assert subAnn != null;
                        
                        var subCmd = loadCommand(cmd, method, subAnn);
                        assert subCmd != null;
                        
                        cmd.addSubCommand(subCmd, subAnn.attachDirectly());
                    }
                } else if (element instanceof ClassElement innerClass) {
                    
                    if (innerClass.isAnnotationPresent(dev.velix.imperat.annotations.Command.class)) {
                        //separate embedded command
                        var innerCmdAnn = innerClass.getAnnotation(dev.velix.imperat.annotations.Command.class);
                        assert innerCmdAnn != null;
                        imperat.registerCommand(
                                loadCommand(null, innerClass, innerCmdAnn)
                        );
                        return null;
                    } else if (innerClass.isAnnotationPresent(SubCommand.class)) {
                        if (cmd == null) {
                            throw new IllegalStateException("Inner class '" + innerClass.getElement().getSimpleName() + "' Cannot be  treated as subcommand, it doesn't have a parent ");
                        }
                        SubCommand subCommandAnn = innerClass.getAnnotation(SubCommand.class);
                        assert subCommandAnn != null;
                        cmd.addSubCommand(
                                loadCommand(cmd, innerClass, subCommandAnn), subCommandAnn.attachDirectly()
                        );
                    }
                    
                }
                
                
            }
            
        }
        
        return cmd;
    }
    
    @SuppressWarnings("unchecked")
    private CommandPreProcessor<S> loadPreProcessorInstance(Class<? extends CommandPreProcessor<?>> clazz) {
        var constructor = Reflections.getConstructor(clazz);
        if (constructor == null)
            throw new UnsupportedOperationException("Couldn't find constructor in class `" + clazz.getSimpleName() + "`");
        
        try {
            return (CommandPreProcessor<S>) constructor.newInstance();
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private CommandPostProcessor<S> loadPostProcessorInstance(Class<? extends CommandPostProcessor<?>> clazz) {
        var constructor = Reflections.getConstructor(clazz);
        if (constructor == null)
            throw new UnsupportedOperationException("Couldn't find constructor in class `" + clazz.getSimpleName() + "`");
        
        try {
            return (CommandPostProcessor<S>) constructor.newInstance();
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    private CommandUsage<S> loadUsage(@Nullable Command<S> parentCmd,
                                      @NotNull Command<S> loadedCmd, MethodElement method) {
        if (method.getInputCount() == 0) {
            loadedCmd.setDefaultUsageExecution(
                    MethodCommandExecutor.of(imperat, method, Collections.emptyList())
            );
            return null;
        }
        
        ClassElement methodOwner = (ClassElement) method.getParent();
        assert methodOwner != null;
        
        var parametersInfo = loadParameters(method, parentCmd);
        
        var execution = MethodCommandExecutor.of(imperat, method, parametersInfo.right());
        
        Description description = method.getAnnotation(Description.class);
        Permission permission = methodOwner.getAnnotation(Permission.class);
        Cooldown cooldown = methodOwner.getAnnotation(Cooldown.class);
        Async async = methodOwner.getAnnotation(Async.class);
        
        var builder = CommandUsage.<S>builder()
                .parameters(parametersInfo.left())
                .execute(execution);
        
        if (description != null)
            builder.description(description.value());
        
        if (permission != null)
            builder.permission(permission.value());
        
        if (cooldown != null)
            builder.cooldown(cooldown.value(), cooldown.unit());
        
        if (async != null)
            builder.coordinator(CommandCoordinator.async());
        
        return builder.build(loadedCmd, method.isHelp());
        
    }
    
    private Pair<List<CommandParameter<S>>, List<CommandParameter<S>>> loadParameters(
            @NotNull MethodElement method,
            @Nullable Command<S> parentCmd
    ) {
        
        LinkedList<CommandParameter<S>> toLoad = new LinkedList<>();
        
        //WHATEVER HAPPENS, NEVER MESS WITH THIS IMPLEMENTATION OR ELSE I WILL FUCKING DESTROY YOU
        final StrictParameterList<S> mainUsageParameters = new StrictParameterList<>();
        Command<S> currentParent = parentCmd;
        while (currentParent != null) {
            currentParent.mainUsage().getParameters()
                    .forEach(mainUsageParameters::addFirst);
            
            currentParent = currentParent.parent();
        }
        
        LinkedList<CommandParameter<S>> total = new LinkedList<>(mainUsageParameters);
        LinkedList<ParameterElement> parameterElements = new LinkedList<>(method.getParameters());
        
        while (!parameterElements.isEmpty()) {
            
            ParameterElement parameterElement = parameterElements.peek();
            if (parameterElement == null) break;
            Type type = parameterElement.getElement().getParameterizedType();
            
            if (imperat.canBeSender(type) || imperat.hasContextResolver(type)) {
                parameterElements.removeFirst();
                continue;
            }
            if (AnnotationHelper.isHelpParameter(parameterElement.getElement())) {
                //CommandHelp parameter
                parameterElements.removeFirst();
                continue;
            }
            
            CommandParameter<S> commandParameter = loadParameter(parameterElement);
            
            CommandParameter<S> mainParameter = mainUsageParameters.peek();
            if (mainParameter == null) {
                toLoad.add(commandParameter);
                total.add(commandParameter);
                parameterElements.removeFirst();
                continue;
            }
            
            if (mainParameter.similarTo(commandParameter)) {
                parameterElements.removeFirst();
                mainUsageParameters.removeFirst();
                continue;
            }
            
            toLoad.add(commandParameter);
            total.add(commandParameter);
            
            mainUsageParameters.removeFirst();
            parameterElements.removeFirst();
        }
        return new Pair<>(toLoad, total);
    }
    
    @SuppressWarnings("unchecked")
    private <T> CommandParameter<S> loadParameter(
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
                || element.isAnnotationPresent(Optional.class)
                || element.isAnnotationPresent(Default.class)
                || element.isAnnotationPresent(DefaultProvider.class);
        
        //reading suggestion annotation
        //element.debug();
        
        Suggest suggestAnnotation = element.getAnnotation(Suggest.class);
        SuggestionProvider suggestionProvider = element.getAnnotation(SuggestionProvider.class);
        
        TypeSuggestionResolver<S, ?> suggestionResolver = null;
        
        if (suggestAnnotation != null) {
            suggestionResolver = SuggestionResolver.type(parameter.getType(), suggestAnnotation.value());
        } else if (suggestionProvider != null) {
            var namedResolver = imperat.getNamedSuggestionResolver(suggestionProvider.value().toLowerCase());
            if (namedResolver != null && !(namedResolver instanceof TypeSuggestionResolver<?, ?>))
                throw new UnsupportedOperationException("Named suggestion resolvers must be of type `TypeSuggestionResolver` and make sure the type matches that of the parameter's");
            else if (namedResolver != null)
                suggestionResolver = (TypeSuggestionResolver<S, ?>) namedResolver;
            else {
                throw new IllegalStateException("Unregistered named suggestion resolver : " + suggestionProvider.value());
            }
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
            Default defaultAnnotation = parameter.getAnnotation(Default.class);
            DefaultProvider provider = parameter.getAnnotation(DefaultProvider.class);
            optionalValueSupplier = AnnotationHelper.deduceOptionalValueSupplier(parameter, defaultAnnotation, provider);
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
        
        CommandParameter<S> param =
                AnnotationParameterDecorator.decorate(
                        CommandParameter.of(
                                name, TypeWrap.of((Class<T>) parameter.getParameterizedType()), permission, desc,
                                optional, greedy, optionalValueSupplier, suggestionResolver
                        ), element
                );
        
        if (TypeUtility.isNumericType(TypeWrap.of(param.type()))
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
        flagAliases.addAll(List.of(array).subList(1, array.length));
        return flagAliases;
    }
    
}
