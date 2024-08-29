package dev.velix.imperat.annotations;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.element.MethodParameterElement;
import dev.velix.imperat.annotations.parameters.AnnotationParameterDecorator;
import dev.velix.imperat.annotations.parameters.NumericParameterDecorator;
import dev.velix.imperat.annotations.types.Description;
import dev.velix.imperat.annotations.types.Permission;
import dev.velix.imperat.annotations.types.methods.*;
import dev.velix.imperat.annotations.types.parameters.Optional;
import dev.velix.imperat.annotations.types.parameters.*;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.cooldown.UsageCooldown;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.NumericRange;
import dev.velix.imperat.command.CommandCoordinator;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.help.MethodHelpExecution;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.annotations.MethodVerifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@ApiStatus.Internal
@SuppressWarnings("unchecked")
final class AnnotationHandlerRegistry<C> extends
        Registry<Class<? extends Annotation>, AnnotationDataInjector<?, C, ?>> {

    // handlers are 2 types
    // 1- Injector (injects data into pre-created objects)
    // 2- Data-Creator (creates the objects to be injected by the injectors)
    private final Map<Class<? extends Annotation>, AnnotationDataCreator<?, ?>> dataCreators = new HashMap<>();

    public AnnotationHandlerRegistry(
            AnnotationRegistry annotationRegistry,
            Imperat<C> dispatcher
    ) {
        super(LinkedHashMap::new);
        registerDataCreator(
                dev.velix.imperat.annotations.types.Command.class,
                (proxyInstance, proxy, commandAnnotation, element) -> {
                    final String[] values = commandAnnotation.value();
                    List<String> aliases = new ArrayList<>(Arrays.asList(values)
                            .subList(1, values.length));
                    Command<C> cmd = Command.createCommand(values[0]);
                    cmd.ignoreACPermissions(commandAnnotation.ignoreAutoCompletionPermission());
                    cmd.addAliases(aliases);

                    return cmd;
                }
        );
        registerDataCreator(Usage.class, (proxyInstance, proxy, annotation, element) -> {
            Method method = (Method) element.getElement();
            var params = loadParameters(annotationRegistry, dispatcher,
                    null, null, proxy, method, false);
            return CommandUsage.<C>builder()
                    .parameters(params)
                    .execute(new MethodCommandExecutor<>(proxyInstance, dispatcher, method, params));
        });

        registerCommandInjector(DefaultUsage.class, (pI, proxy, command, toLoad, element, annotation) -> {
            Method method = (Method) element.getElement();
            if (element.isAnnotationPresent(Usage.class) || element.isAnnotationPresent(SubCommand.class)) {
                throw new IllegalArgumentException("A default usage in method '" + method.getName() + "' in class '" + proxy.getName() + "'");
            }

            command.setDefaultUsageExecution(new MethodCommandExecutor<>(pI, dispatcher, method,
                    Collections.emptyList()));
        });


        registerSubCmdInjector(annotationRegistry, dispatcher);

        registerCommandInjector(Help.class, ((pI, proxy, command, toLoad, element, annotation) -> {
            MethodVerifier.verifyHelpMethod(dispatcher, toLoad.getMainUsage(), proxy, (Method) element.getElement());
            Method method = (Method) element.getElement();
            var subCommandParams = loadParameters(annotationRegistry, dispatcher, null, toLoad.getMainUsage(), proxy, method, true);

            List<CommandParameter> parameters = new ArrayList<>(command.getMainUsage().getParameters());
            parameters.addAll(subCommandParams);

            toLoad.addHelpCommand(dispatcher, subCommandParams,
                    new MethodHelpExecution<>(dispatcher, pI, method, parameters));
        }));

        registerUsageInjector(Async.class,
                (pI, proxy, command, toLoad, element, annotation) -> toLoad.coordinator(CommandCoordinator.async()));

        registerUsageInjector(Cooldown.class, (pI, proxy, command, toLoad, element, annotation)
                -> toLoad.cooldown(loadCooldown(element)));

        registerInjector(Permission.class, (pI, proxy, command, toLoad, element, annotation) -> {
            command.setPermission(annotation.value());
            if (toLoad instanceof CommandUsage.Builder<?> builder) {
                builder.permission(annotation.value());
            } else {
                command.setPermission(annotation.value());
            }
        });

        registerInjector(Description.class, ((pI, proxy, command, toLoad, element, annotation) -> {
            command.setDescription(annotation.value());
            if (toLoad instanceof CommandUsage.Builder<?> usage) {
                usage.description(annotation.value());
            } else {
                command.setDescription(annotation.value());
            }
        }));
    }

    private void registerSubCmdInjector(AnnotationRegistry annotationRegistry, Imperat<C> dispatcher) {
        registerCommandInjector(SubCommand.class, (pI, proxy, command, toLoad, element, annotation) -> {
            Method method = (Method) element.getElement();

            if (element.isAnnotationPresent(Usage.class)) {
                logError(method, proxy, "@Usage and @SubCommand cannot be used together (only one of them is allowed per method)");
                return;
            }
            if (element.isAnnotationPresent(Help.class)) {
                logError(method, proxy, "@SubCommand cannot be treated as a help , @Help was used with @SubCommand");
                return;
            }

            final String[] values = annotation.value();
            List<String> aliases = new ArrayList<>(Arrays.asList(values)
                    .subList(1, values.length));

            var mainUsage = command.getMainUsage();
            List<CommandParameter> methodCommandParameters = this.loadParameters(annotationRegistry, dispatcher, annotation, mainUsage, proxy, method, false);

            //merging the command's main usage params with subcommand's parameters to use them in the method to be invoked
            List<CommandParameter> fullParams = new ArrayList<>(mainUsage.getParameters().size() + methodCommandParameters.size());
            fullParams.addAll(mainUsage.getParameters());
            fullParams.addAll(methodCommandParameters);

            UsageCooldown cooldown = loadCooldown(element);
            String desc = element.isAnnotationPresent(Description.class) ? element.getAnnotation(Description.class).value() : "N/A";
            String permission = element.isAnnotationPresent(Permission.class) ? element.getAnnotation(Permission.class).value() : null;
            toLoad.addSubCommandUsage(values[0], aliases,
                    CommandUsage.<C>builder()
                            .parameters(methodCommandParameters)
                            .coordinator(element.isAnnotationPresent(Async.class)
                                    ? CommandCoordinator.async() : CommandCoordinator.sync())
                            .description(desc)
                            .permission(permission)
                            .cooldown(cooldown)
                            .execute(new MethodCommandExecutor<>(pI, dispatcher, method,
                                    fullParams)).build(),
                    annotation.attachDirectly()
            );
        });
    }

    private UsageCooldown loadCooldown(CommandAnnotatedElement<?> element) {
        if (!element.isAnnotationPresent(Cooldown.class)) return null;
        var cooldownAnnotation = element.getAnnotation(Cooldown.class);
        return new UsageCooldown(cooldownAnnotation.value(), cooldownAnnotation.unit());
    }

    public <A extends Annotation> void registerDataCreator(Class<A> annotationType, AnnotationDataCreator<?, A> creator) {
        dataCreators.put(annotationType, creator);
    }

    @SuppressWarnings("unchecked")
    public @Nullable <A extends Annotation, O> AnnotationDataCreator<O, A> getDataCreator(Class<A> annotationClass) {
        return (AnnotationDataCreator<O, A>) dataCreators.get(annotationClass);
    }

    public <A extends Annotation, O> void registerInjector(
            Class<A> aClass,
            AnnotationDataInjector<O, C, A> injector
    ) {
        setData(aClass, injector);
    }

    public <A extends Annotation> void registerUsageInjector(Class<A> aClass, AnnotationDataInjector<CommandUsage.Builder<C>, C, A> injector) {
        this.registerInjector(aClass, injector);
    }

    public <A extends Annotation> void registerCommandInjector(Class<A> aClass, AnnotationDataInjector<Command<C>, C, A> injector) {
        this.registerInjector(aClass, injector);
    }


    @SuppressWarnings("unchecked")
    public @Nullable <A extends Annotation> AnnotationDataInjector<?, C, A> getInjector(Class<A> clazz) {
        return (AnnotationDataInjector<?, C, A>) getData(clazz).orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <A extends Annotation, O> void injectForElement(
            @NotNull Object proxyInstance,
            @NotNull Class<?> proxy,
            @NotNull Command<C> command,
            @NotNull O toLoad,
            @NotNull CommandAnnotatedElement<?> element
    ) {

        for (Annotation annotation : element) {
            AnnotationDataInjector<O, C, A> injector =
                    (AnnotationDataInjector<O, C, A>) getInjector(annotation.annotationType());
            if (injector == null) continue;
            injector.inject(proxyInstance, proxy, command, toLoad,
                    element, (A) annotation);
        }

    }

    private List<CommandParameter> loadParameters(AnnotationRegistry registry,
                                                  Imperat<C> dispatcher,
                                                  @Nullable SubCommand subCommand,
                                                  @Nullable CommandUsage<C> mainUsage,
                                                  Class<?> proxyClass,
                                                  Method method,
                                                  boolean help) {

        List<CommandParameter> commandParameters = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            if (dispatcher.canBeSender(parameter.getType())) continue;
            if (help && CommandHelp.class.isAssignableFrom(parameter.getType())) {
                continue;
            }

            CommandParameter commandParameter = getParameter(dispatcher, registry, proxyClass, method, parameter);

            if (dispatcher.hasContextResolver(parameter.getType())) continue;

            if (subCommand != null || help) {
                if (mainUsage != null && mainUsage
                        .hasParameter((param) -> param.equals(commandParameter))) {
                    continue;
                }
            }

            commandParameters.add(commandParameter);
        }
        return commandParameters;
    }

    //TODO make a parameter injector
    private <T> CommandParameter getParameter(
            Imperat<C> dispatcher,
            AnnotationRegistry registry,
            Class<?> proxyClass,
            Method method,
            Parameter parameter
    ) {

        Named named = parameter.getAnnotation(Named.class);
        Flag flag = parameter.getAnnotation(Flag.class);
        Switch switchAnnotation = parameter.getAnnotation(Switch.class);

        if (flag != null && switchAnnotation != null) {
            logError(method, proxyClass, "both @Flag and @Switch at the same time !");
            return null;
        }

        String name = getParamName(parameter, named, flag, switchAnnotation);
        MethodParameterElement element = new MethodParameterElement(registry, name, parameter);
        boolean optional = flag != null || switchAnnotation != null
                || element.isAnnotationPresent(Optional.class);

        //reading suggestions annotation
        Suggest suggestAnnotation = element.getAnnotation(Suggest.class);
        SuggestionProvider suggestionProvider = element.getAnnotation(SuggestionProvider.class);

        SuggestionResolver<C, T> suggestionResolver = null;

        if (suggestAnnotation != null) {
            suggestionResolver = (SuggestionResolver<C, T>) SuggestionResolver.plain(parameter.getType(), suggestAnnotation.value());
        } else if (suggestionProvider != null) {
            suggestionResolver = dispatcher.getNamedSuggestionResolver(suggestionProvider.value().toLowerCase());
        }

        boolean greedy = parameter.getAnnotation(Greedy.class) != null;

        if (greedy && parameter.getType() != String.class) {
            throw new IllegalArgumentException("Argument '" + parameter.getName() + "' is greedy while having a non-greedy type '" + parameter.getType().getName() + "'");
        }

        OptionalValueSupplier<T> optionalValueSupplier = null;
        if (optional) {
            DefaultValue defaultValueAnnotation = parameter.getAnnotation(DefaultValue.class);
            DefaultValueProvider provider = parameter.getAnnotation(DefaultValueProvider.class);
            optionalValueSupplier = deduceOptionalValueSupplier(parameter, defaultValueAnnotation, provider);
        }

        if (flag != null) {
            String[] flagAliases = flag.value();
            return AnnotationParameterDecorator.decorate(
                    CommandParameter.flag(name, getAllExceptFirst(flagAliases), flag.inputType(), optionalValueSupplier),
                    element
            );
        } else if (switchAnnotation != null) {
            String[] switchAliases = switchAnnotation.value();
            return AnnotationParameterDecorator.decorate(
                    CommandParameter.switchParam(name, getAllExceptFirst(switchAliases)),
                    element
            );
        }

        CommandParameter param =
                AnnotationParameterDecorator.decorate(
                        CommandParameter.of(
                                name, (Class<T>) parameter.getType(),
                                optional, greedy, optionalValueSupplier, suggestionResolver
                        ), element
                );

        if (TypeUtility.isNumericType(param.getType())
                && element.isAnnotationPresent(Range.class)) {
            Range annotation = element.getAnnotation(Range.class);
            param = NumericParameterDecorator.decorate(
                    param, NumericRange.of(annotation.min(), annotation.max())
            );
        }

        return param;
    }

    private List<String> getAllExceptFirst(String[] array) {
        List<String> flagAliases = new ArrayList<>(array.length - 1);
        flagAliases.addAll(Arrays.asList(array).subList(1, array.length));
        return flagAliases;
    }


    private static <T> OptionalValueSupplier<T> deduceOptionalValueSupplier(
            Parameter parameter,
            DefaultValue defaultValueAnnotation,
            DefaultValueProvider provider
    ) {

        if (defaultValueAnnotation != null) {
            String def = defaultValueAnnotation.value();
            return (OptionalValueSupplier<T>) OptionalValueSupplier.of(def);
        } else if (provider != null) {
            Class<? extends OptionalValueSupplier<?>> supplierClass = provider.value();
            try {
                return getOptionalValueSupplier(parameter, supplierClass);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new IllegalAccessError("Optional value suppler class '" +
                        supplierClass.getName() + "' doesn't have an empty accessible constructor !");
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    private static <T> @NotNull OptionalValueSupplier<T> getOptionalValueSupplier(
            Parameter parameter,
            Class<? extends OptionalValueSupplier<?>> supplierClass
    ) throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException {

        var emptyConstructor = supplierClass.getDeclaredConstructor();
        emptyConstructor.setAccessible(true);
        OptionalValueSupplier<T> valueSupplier = (OptionalValueSupplier<T>) emptyConstructor.newInstance();
        if (!TypeUtility.matches(valueSupplier.getValueType(), parameter.getType())) {
            throw new IllegalArgumentException("Optional supplier of value-type '" + valueSupplier.getValueType().getName() + "' doesn't match the optional arg type '" + parameter.getType().getName() + "'");
        }

        return valueSupplier;
    }

    private static void logError(Method method, Class<?> proxy, String msg) {
        throw new IllegalArgumentException(String.format(msg + " in method '%s' of class '%s'", method.getName(), proxy.getName()));
    }

    private static String getParamName(
            Parameter parameter,
            @Nullable Named named,
            @Nullable Flag flag,
            @Nullable Switch switchAnnotation
    ) {
        String name;

        if (named != null)
            name = named.value();
        else if (flag != null)
            name = flag.value()[0];
        else if (switchAnnotation != null)
            name = switchAnnotation.value()[0];
        else
            name = parameter.getName();

        return name;
    }

}


