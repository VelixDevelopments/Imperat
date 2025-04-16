package dev.velix.imperat.annotations.base.element;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.ImperatConfig;
import dev.velix.imperat.annotations.Async;
import dev.velix.imperat.annotations.Cooldown;
import dev.velix.imperat.annotations.Default;
import dev.velix.imperat.annotations.DefaultProvider;
import dev.velix.imperat.annotations.Description;
import dev.velix.imperat.annotations.Flag;
import dev.velix.imperat.annotations.Greedy;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.Optional;
import dev.velix.imperat.annotations.Permission;
import dev.velix.imperat.annotations.PostProcessor;
import dev.velix.imperat.annotations.PreProcessor;
import dev.velix.imperat.annotations.Range;
import dev.velix.imperat.annotations.SubCommand;
import dev.velix.imperat.annotations.Suggest;
import dev.velix.imperat.annotations.SuggestionProvider;
import dev.velix.imperat.annotations.Switch;
import dev.velix.imperat.annotations.Usage;
import dev.velix.imperat.annotations.Values;
import dev.velix.imperat.annotations.base.AnnotationHelper;
import dev.velix.imperat.annotations.base.AnnotationParser;
import dev.velix.imperat.annotations.base.MethodCommandExecutor;
import dev.velix.imperat.annotations.base.element.selector.ElementSelector;
import dev.velix.imperat.annotations.parameters.AnnotationParameterDecorator;
import dev.velix.imperat.annotations.parameters.NumericParameterDecorator;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandCoordinator;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.ConstrainedParameterTypeDecorator;
import dev.velix.imperat.command.parameters.NumericRange;
import dev.velix.imperat.command.parameters.OptionalValueSupplier;
import dev.velix.imperat.command.parameters.StrictParameterList;
import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.context.FlagData;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import dev.velix.imperat.util.reflection.Reflections;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApiStatus.Internal
final class SimpleCommandClassVisitor<S extends Source> extends CommandClassVisitor<S> {


    private final ImperatConfig<S> config;
    private final static String VALUES_SEPARATION_CHAR = "\\|";

    SimpleCommandClassVisitor(Imperat<S> imperat, AnnotationParser<S> parser, ElementSelector<MethodElement> methodSelector) {
        super(imperat, parser, methodSelector);
        this.config = imperat.config();
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

            if(clazz.isRootClass() && AnnotationHelper.isAbnormalClass(clazz)) {
                throw new IllegalArgumentException("Abnormal root class '%s'".formatted(clazz.getName()));
            }

            Command<S> cmd = loadCommand(null, clazz, commandAnnotation);

            //if cmd=null → loading @CommandProcessingChain methods only from this class
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
            final String[] values = config.replacePlaceholders(cmdAnn.value());
            final List<String> aliases = List.of(values).subList(1, values.length);
            final boolean ignoreAC = cmdAnn.skipSuggestionsChecks();

            var builder = Command.<S>create(values[0])
                .ignoreACPermissions(ignoreAC)
                .aliases(aliases);
            if (permission != null) {
                builder.permission(
                    config.replacePlaceholders(permission.value())
                );
            }

            if (description != null) {
                builder.description(
                    config.replacePlaceholders(description.value())
                );
            }

            if (preProcessor != null) {
                builder.preProcessor(loadPreProcessorInstance(preProcessor.value()));
            }

            if (postProcessor != null) {
                builder.postProcessor(loadPostProcessorInstance(postProcessor.value()));
            }

            return builder.build();

        } else if (cmdAnnotation instanceof SubCommand subCommand) {
            final String[] values = config.replacePlaceholders(subCommand.value());
            assert values != null;

            final List<String> aliases = List.of(values).subList(1, values.length);
            final boolean ignoreAC = subCommand.skipSuggestionsChecks();

            var builder = Command.<S>create(values[0])
                .ignoreACPermissions(ignoreAC)
                .aliases(aliases);

            if (permission != null) {
                builder.permission(
                    config.replacePlaceholders(permission.value())
                );
            }

            if (description != null) {
                builder.description(
                    config.replacePlaceholders(description.value())
                );
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

    private @Nullable Command<S> loadCommand(
        @Nullable Command<S> parentCmd,
        ParseElement<?> parseElement,
        @NotNull Annotation annotation
    ) {
        if(AnnotationHelper.isAbnormalClass(parseElement)) {
            //sub abnormal class
            //ignore
            return null;
        }

        final Command<S> cmd = loadCmdInstance(annotation, parseElement);
        if (parentCmd != null && cmd != null) {
            cmd.parent(parentCmd);
        }
        if (parseElement instanceof MethodElement method && cmd != null) {
            //@CommandProcessingChain on method
            if (!methodSelector.canBeSelected(imperat, parser, method, true)) {
                ImperatDebugger.debugForTesting("Method '%s' has failed verification", method.getName());
                return cmd;
            }

            var usage = loadUsage(parentCmd, cmd, method);

            if (usage != null) {
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

                    if (!methodSelector.canBeSelected(imperat, parser, method, true)) {
                        return cmd;
                    }

                    if (method.isAnnotationPresent(Usage.class)) {

                        var usage = loadUsage(parentCmd, cmd, method);
                        if (usage != null) {
                            cmd.addUsage(usage);
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


    private CommandUsage<S> loadUsage(
        @Nullable Command<S> parentCmd,
        @NotNull Command<S> loadedCmd,
        MethodElement method
    ) {
        boolean isAttachedDirectly = isIsAttachedDirectlySubCmd(method);

        int inputCount = method.getInputCount();
        if (parentCmd != null) {
            int parentalParams = 0;

            if (!isAttachedDirectly) {
                ImperatDebugger.debugForTesting("Going in deep for method '%s'", method.getName() );
                Command<S> parent = parentCmd;
                while (parent != null) {
                    parentalParams += parent.mainUsage().size();
                    parent = parent.parent();
                }
            }

            inputCount = Math.abs(method.getInputCount() - parentalParams);
        }

        ImperatDebugger.debugForTesting("The method-usage '%s', has '%s' calculated input count", method.getName(), inputCount);
        if (inputCount == 0) {
            if (parentCmd != null && !isAttachedDirectly) {
                assert method.getParent() != null;
                MethodUsageData<S> usageData = loadParameters(method, parentCmd);
                loadedCmd.setDefaultUsageExecution(
                    MethodCommandExecutor.of(imperat, method, usageData.inheritedTotalParameters())
                );

            } else {
                loadedCmd.setDefaultUsageExecution(
                    MethodCommandExecutor.of(imperat, method, Collections.emptyList())
                );
            }
            Cooldown cooldown = method.getAnnotation(Cooldown.class);
            Async async = method.getAnnotation(Async.class);

            if(cooldown != null) {
                String cooldownPerm = cooldown.permission();
                loadedCmd.getDefaultUsage().setCooldown(cooldown.value(), cooldown.unit(), cooldownPerm.isEmpty() ? null : cooldownPerm);
            }
            if(async != null) {
                loadedCmd.getDefaultUsage().setCoordinator(CommandCoordinator.async());
            }

            return null;
        }

        MethodUsageData<S> usageData = loadParameters(method, parentCmd);

        var execution = MethodCommandExecutor.of(imperat, method, usageData.inheritedTotalParameters());

        Description description = method.getAnnotation(Description.class);
        Permission permission = method.getAnnotation(Permission.class);
        Cooldown cooldown = method.getAnnotation(Cooldown.class);
        Async async = method.getAnnotation(Async.class);

        var builder = CommandUsage.<S>builder()
            .parameters(usageData.personalParameters())
            .execute(execution);

        if (description != null)
            builder.description(
                config.replacePlaceholders(description.value())
            );

        if (permission != null)
            builder.permission(
                config.replacePlaceholders(permission.value())
            );

        if (cooldown != null) {
            ImperatDebugger.debug("Method '%s' has cooldown", method.getName());
            String cooldownPerm = cooldown.permission();
            builder.cooldown(cooldown.value(), cooldown.unit(), cooldownPerm.isEmpty() ? null : cooldownPerm);
        }

        if (async != null)
            builder.coordinator(CommandCoordinator.async());

        return builder
            .registerFlags(usageData.freeFlags)
            .build(loadedCmd, method.isHelp());

    }

    private MethodUsageData<S> loadParameters(
        @NotNull MethodElement method,
        @Nullable Command<S> parentCmd
    ) {

        ImperatDebugger.debugForTesting("Loading for method '%s'", method.getName());
        LinkedList<CommandParameter<S>> toLoad = new LinkedList<>();

        final StrictParameterList<S> mainUsageParameters = new StrictParameterList<>();

        boolean isAttachedDirectlySubCmd = isIsAttachedDirectlySubCmd(method);

        if (!isAttachedDirectlySubCmd) {
            LinkedList<Command<S>> parenteralSequence = getParenteralSequence(parentCmd);
            for(Command<S> parent : parenteralSequence) {
                parent.mainUsage().getParameters()
                        .forEach((param) -> {
                            if (!(param.isFlag() && param.asFlagParameter().flagData().isFree())) {
                                mainUsageParameters.add(param);
                            }
                        });
            }

        }

        var inheritedParamsFormatted = getMainUsageParametersCollected(mainUsageParameters);
        ImperatDebugger.debugForTesting("Main usage params collected '%s'", inheritedParamsFormatted.toString());

        LinkedList<CommandParameter<S>> total = new LinkedList<>(mainUsageParameters);
        LinkedList<ParameterElement> methodParameters = new LinkedList<>(method.getParameters());
        ImperatDebugger.debugForTesting("Method parameters collected '%s'", getMethodParamsCollected(methodParameters));

        Set<FlagData<S>> freeFlags = new HashSet<>();

        ParameterElement senderParam = null;

        if(!isAttachedDirectlySubCmd && methodParameters.size()-1 == 0 && !mainUsageParameters.isEmpty() && parentCmd != null) {
            assert method.getParent() != null;
            throw new IllegalStateException("You have inherited parameters ('%s') that are not declared in the method '%s' in class '%s'".formatted(inheritedParamsFormatted, method.getName(), method.getParent().getName()));
        }

        while (!methodParameters.isEmpty()) {

            ParameterElement parameterElement = methodParameters.peek();
            if (parameterElement == null) break;
            Type type = parameterElement.getElement().getParameterizedType();
            if ( (senderParam == null && (isSenderParameter(parameterElement))) || config.hasContextResolver(type)) {
                senderParam = methodParameters.remove();
                continue;
            }

            CommandParameter<S> commandParameter = loadParameter(parameterElement);
            if (commandParameter.isFlag() && commandParameter.asFlagParameter().flagData().isFree()) {
                freeFlags.add(commandParameter.asFlagParameter().flagData());
                methodParameters.remove();
                continue;
            }

            CommandParameter<S> mainParameter = mainUsageParameters.peek();
            if(mainParameter != null)
                ImperatDebugger.debugForTesting("Comparing main-usage parameter '%s' with loaded parameter '%s'", mainParameter.format(), commandParameter.format());


            if (mainParameter == null) {
                ImperatDebugger.debugForTesting("Adding command parameter '%s' that has no corresponding main parameter", commandParameter.format());
                toLoad.add(commandParameter);
                total.add(commandParameter);
                methodParameters.remove();
                continue;
            }

            if (mainParameter.similarTo(commandParameter)) {
                ImperatDebugger.debugForTesting("Main parameter '%s' is exactly similar to loaded parameter '%s'", mainParameter.format(), commandParameter.format());
                var methodParam = methodParameters.remove();
                ImperatDebugger.debugForTesting("Removing '%s' from method params", methodParam.getName());
                var mainUsageParam = mainUsageParameters.remove();
                ImperatDebugger.debugForTesting("Removing '%s' from main usage params", mainUsageParam.format());
                continue;
            }

            toLoad.add(commandParameter);
            total.add(commandParameter);

            mainUsageParameters.remove();
            methodParameters.remove();
        }
        return new MethodUsageData<>(toLoad, total, freeFlags);
    }

    private static boolean isIsAttachedDirectlySubCmd(@NotNull MethodElement method) {
        boolean isAttachedDirectlySubCmd = false;
        if(method.isAnnotationPresent(SubCommand.class)) {
            isAttachedDirectlySubCmd = Objects.requireNonNull(method.getAnnotation(SubCommand.class)).attachDirectly();
        } else if (method.isAnnotationPresent(Usage.class)) {
            assert method.getParent() != null;
            var ann =  method.getParent().getAnnotation(SubCommand.class);
            if(ann != null) {
                isAttachedDirectlySubCmd = ann.attachDirectly();
            }
        }
        return isAttachedDirectlySubCmd;
    }

    private boolean isSenderParameter(ParameterElement parameter) {
        Type type = parameter.getElement().getParameterizedType();
        return imperat.canBeSender(type) || config.hasSourceResolver(type);
    }

    private String getMethodParamsCollected(LinkedList<ParameterElement> methodParameters) {

        StringBuilder builder = new StringBuilder();
        for(var pe : methodParameters) {
            builder.append(pe.getName()).append(" ");
        }
        return builder.toString();
    }

    private static <S extends Source> @NotNull StringBuilder getMainUsageParametersCollected(StrictParameterList<S> mainUsageParameters) {
        StringBuilder builder = new StringBuilder();
        for(var p : mainUsageParameters) {
            builder.append(p.format()).append(" ");
        }
        return builder;
    }

    private static <S extends Source> @NotNull LinkedList<Command<S>> getParenteralSequence(@Nullable Command<S> parentCmd) {
        Command<S> currentParent = parentCmd;
        LinkedList<Command<S>> parenteralSequence = new LinkedList<>();
        while (currentParent != null) {
            parenteralSequence.addFirst(currentParent);
            currentParent = currentParent.parent();
        }
        return parenteralSequence;
    }

    @SuppressWarnings("unchecked")
    private <T> CommandParameter<S> loadParameter(
        @NotNull ParameterElement parameter
    ) {

        //Parameter parameter = element.getElement();

        Named named = parameter.getAnnotation(Named.class);
        Flag flag = parameter.getAnnotation(Flag.class);
        Switch switchAnnotation = parameter.getAnnotation(Switch.class);

        if (flag != null && switchAnnotation != null) {
            throw new IllegalStateException("both @Flag and @Switch at the same time !");
        }

        TypeWrap<T> parameterTypeWrap = (TypeWrap<T>) TypeWrap.of(parameter.getElement().getParameterizedType());
        var type = (ParameterType<S, T>) config.getParameterType(parameterTypeWrap.getType());
        if (type == null) {
            throw new IllegalArgumentException("Unknown type detected '" + parameterTypeWrap.getType().getTypeName() + "'");
        }

        String name = AnnotationHelper.getParamName(config, parameter, named, flag, switchAnnotation);
        boolean optional = flag != null || switchAnnotation != null
            || parameter.isAnnotationPresent(Optional.class)
            || parameter.isAnnotationPresent(Default.class)
            || parameter.isAnnotationPresent(DefaultProvider.class);

        //reading suggestion annotation
        //element.debug();

        Suggest suggestAnnotation = parameter.getAnnotation(Suggest.class);
        SuggestionProvider suggestionProvider = parameter.getAnnotation(SuggestionProvider.class);

        SuggestionResolver<S> suggestionResolver = null;

        if (suggestAnnotation != null) {
            suggestionResolver = SuggestionResolver.plain(
                config.replacePlaceholders(suggestAnnotation.value())
            );
        } else if (suggestionProvider != null) {
            String suggestionResolverName = config.replacePlaceholders(suggestionProvider.value().toLowerCase());
            var namedResolver = config.getNamedSuggestionResolver(
                suggestionResolverName
            );
            if (namedResolver != null)
                suggestionResolver = namedResolver;
            else {
                throw new IllegalStateException("Unregistered named suggestion resolver : " + suggestionResolverName);
            }
        }

        boolean greedy = parameter.getAnnotation(Greedy.class) != null;

        if (greedy && parameter.getType() != String.class) {
            throw new IllegalArgumentException("Argument '" + parameter.getName() + "' is greedy while having a non-greedy valueType '" + parameter.getType().getTypeName() + "'");
        }

        dev.velix.imperat.command.Description desc = dev.velix.imperat.command.Description.EMPTY;
        if (parameter.isAnnotationPresent(Description.class)) {
            var descAnn = parameter.getAnnotation(Description.class);
            assert descAnn != null;
            desc = dev.velix.imperat.command.Description.of(
                config.replacePlaceholders(descAnn.value())
            );
        }

        String permission = null;
        if (parameter.isAnnotationPresent(Permission.class)) {
            var permAnn = parameter.getAnnotation(Permission.class);
            assert permAnn != null;
            permission = config.replacePlaceholders(permAnn.value());
        }

        OptionalValueSupplier optionalValueSupplier = OptionalValueSupplier.empty();
        if (optional) {
            Default defaultAnnotation = parameter.getAnnotation(Default.class);
            DefaultProvider provider = parameter.getAnnotation(DefaultProvider.class);
            try {
                optionalValueSupplier = AnnotationHelper.deduceOptionalValueSupplier(imperat, parameter, defaultAnnotation, provider, optionalValueSupplier);
            } catch (ImperatException e) {
                ImperatDebugger.error(AnnotationHelper.class, "deduceOptionalValueSupplier", e);
            }
        }

        if (flag != null) {
            String[] flagAliases = flag.value();
            if (suggestAnnotation != null) {
                suggestionResolver = SuggestionResolver.plain(config.replacePlaceholders(suggestAnnotation.value()));
            }

            return AnnotationParameterDecorator.decorate(
                CommandParameter.flag(name, type)
                    .setFree(flag.free())
                    .suggestForInputValue(suggestionResolver)
                    .aliases(getAllExceptFirst(flagAliases))
                    .flagDefaultInputValue(optionalValueSupplier)
                    .description(desc)
                    .permission(permission)
                    .build(),
                parameter
            );
        } else if (switchAnnotation != null) {
            String[] switchAliases = switchAnnotation.value();
            return AnnotationParameterDecorator.decorate(
                CommandParameter.<S>flagSwitch(name)
                    .setFree(switchAnnotation.free())
                    .aliases(getAllExceptFirst(switchAliases))
                    .description(desc)
                    .permission(permission)
                    .build(),
                parameter
            );
        }


        if(parameter.isAnnotationPresent(Values.class)) {
            Values valuesAnnotation = parameter.getAnnotation(Values.class);
            assert valuesAnnotation != null;

            Set<String> values = Arrays.stream(valuesAnnotation.value())
                    .distinct()
                    .map(config::replacePlaceholders)
                    .flatMap(replaced -> {
                        if (replaced.contains("|")) {
                            return Arrays.stream(replaced.split(VALUES_SEPARATION_CHAR));
                        } else {
                            return Stream.of(replaced);
                        }
                    })
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            type = ConstrainedParameterTypeDecorator.of(type, values, valuesAnnotation.caseSensitive());
        }

        ImperatDebugger.debug("Optional value is empty='%s'", optionalValueSupplier.isEmpty());
        CommandParameter<S> param =
            AnnotationParameterDecorator.decorate(
                CommandParameter.of(
                    name, type, permission, desc,
                    optional, greedy, optionalValueSupplier, suggestionResolver
                ), parameter
            );

        if (TypeUtility.isNumericType(TypeWrap.of(param.valueType()))
            && parameter.isAnnotationPresent(Range.class)) {
            Range range = parameter.getAnnotation(Range.class);
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

    private record MethodUsageData<S extends Source>(
        List<CommandParameter<S>> personalParameters,
        List<CommandParameter<S>> inheritedTotalParameters,
        Set<FlagData<S>> freeFlags
    ) {

    }
}
