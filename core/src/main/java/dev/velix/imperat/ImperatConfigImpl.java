package dev.velix.imperat;

import dev.velix.imperat.annotations.base.element.ParameterElement;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.ContextResolverFactory;
import dev.velix.imperat.command.ContextResolverRegistry;
import dev.velix.imperat.command.SourceResolverRegistry;
import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.command.processors.CommandProcessingChain;
import dev.velix.imperat.command.processors.impl.DefaultProcessors;
import dev.velix.imperat.command.suggestions.SuggestionResolverRegistry;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ParamTypeRegistry;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.ContextFactory;
import dev.velix.imperat.exception.CooldownException;
import dev.velix.imperat.exception.InvalidSourceException;
import dev.velix.imperat.exception.InvalidSyntaxException;
import dev.velix.imperat.exception.InvalidUUIDException;
import dev.velix.imperat.exception.NoHelpException;
import dev.velix.imperat.exception.NoHelpPageException;
import dev.velix.imperat.exception.PermissionDeniedException;
import dev.velix.imperat.exception.SelfHandledException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.exception.ThrowableResolver;
import dev.velix.imperat.help.HelpProvider;
import dev.velix.imperat.placeholders.Placeholder;
import dev.velix.imperat.placeholders.PlaceholderRegistry;
import dev.velix.imperat.placeholders.PlaceholderResolver;
import dev.velix.imperat.resolvers.ContextResolver;
import dev.velix.imperat.resolvers.DependencySupplier;
import dev.velix.imperat.resolvers.PermissionResolver;
import dev.velix.imperat.resolvers.SourceResolver;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.Preconditions;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.verification.UsageVerifier;
import org.jetbrains.annotations.*;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

final class ImperatConfigImpl<S extends Source> implements ImperatConfig<S> {

    private @NotNull PermissionResolver<S> permissionResolver = (source, permission) -> true;
    private @NotNull ContextFactory<S> contextFactory;
    private @NotNull UsageVerifier<S> verifier;
    private @Nullable HelpProvider<S> provider = null;

    private final Registry<Type, DependencySupplier> dependencyResolverRegistry = new Registry<>();

    private final ContextResolverRegistry<S> contextResolverRegistry;
    private final ParamTypeRegistry<S> paramTypeRegistry;
    private final SuggestionResolverRegistry<S> suggestionResolverRegistry;
    private final PlaceholderRegistry<S> placeholderRegistry;
    private final SourceResolverRegistry<S> sourceResolverRegistry;

    private final Map<Class<? extends Throwable>, ThrowableResolver<?, S>> handlers = new HashMap<>();

    private @NotNull CommandProcessingChain<S, CommandPreProcessor<S>> globalPreProcessors;
    private @NotNull CommandProcessingChain<S, CommandPostProcessor<S>> globalPostProcessors;

    private boolean strictCommandTree = false;

    private String commandPrefix = "/";

    ImperatConfigImpl() {
        contextResolverRegistry = ContextResolverRegistry.createDefault(this);
        paramTypeRegistry = ParamTypeRegistry.createDefault();
        suggestionResolverRegistry = SuggestionResolverRegistry.createDefault(this);
        sourceResolverRegistry = SourceResolverRegistry.createDefault();
        placeholderRegistry = PlaceholderRegistry.createDefault(this);
        contextFactory = ContextFactory.defaultFactory();

        verifier = UsageVerifier.typeTolerantVerifier();
        regDefThrowableResolvers();

        globalPreProcessors = CommandProcessingChain.<S>preProcessors()
            .then(DefaultProcessors.preUsagePermission())
            .then(DefaultProcessors.preUsageCooldown())
            .build();

        globalPostProcessors = CommandProcessingChain.<S>postProcessors()
            .build();
    }

    private void regDefThrowableResolvers() {

        this.setThrowableResolver(InvalidSourceException.class, (exception, imperat, context) -> {
            throw new UnsupportedOperationException("Couldn't find any source resolver for valueType `"
                + exception.getTargetType().getTypeName() + "'");
        });

        this.setThrowableResolver(
            SourceException.class,
            (exception, imperat, context) -> {
                final String msg = exception.getMessage();
                switch (exception.getType()) {
                    case SEVERE -> context.source().error(msg);
                    case WARN -> context.source().warn(msg);
                    case REPLY -> context.source().reply(msg);
                }
            }
        );

        this.setThrowableResolver(
            InvalidUUIDException.class, (exception, imperat, context) ->
                context.source().error("Invalid uuid-format '" + exception.getRaw() + "'")
        );

        this.setThrowableResolver(
            CooldownException.class,
            (exception, imperat, context) -> {
                final long lastTimeExecuted = exception.getCooldown();
                final long timePassed = System.currentTimeMillis() - lastTimeExecuted;
                final long remaining = exception.getDefaultCooldown() - timePassed;
                context.source().error(
                    "Please wait %d second(s) to execute this command again!".formatted(remaining)
                );
            }
        );
        this.setThrowableResolver(
            PermissionDeniedException.class,
            (exception, imperat, context) -> context.source().error("You don't have permission to use this command!")
        );
        this.setThrowableResolver(
            InvalidSyntaxException.class,
            (exception, imperat, context) -> {
                S source = context.source();
                //if usage is null, find the closest usage

                List<CommandUsage<S>> closestUsages = new ArrayList<>();
                if(context instanceof ResolvedContext<S> resolvedContext) {
                    closestUsages.add(resolvedContext.getDetectedUsage());
                }else {
                    var cmd = context.command();
                    closestUsages.addAll(
                            cmd.findUsages((usage)-> usage.size() > context.arguments().size())
                    );
                }

                source.error(
                    "Invalid command usage '<raw_args>'".replace("<raw_args>", "/" + context.command().name() + " " + context.arguments().join(" "))
                );

                if (closestUsages.isEmpty()) {
                    return;
                }

                StringBuilder possibleUsages = new StringBuilder();
                int i = 0;
                for(var usage : closestUsages) {

                    possibleUsages.append("- ").append(imperat.commandPrefix()).append(CommandUsage.format(context.label(), usage));
                    if(i != closestUsages.size()) {
                        possibleUsages.append("\n");
                    }
                    i++;
                }

                assert !possibleUsages.isEmpty();
                source.error(
                        "Possible Command Usages: " +
                        "\n" +
                        possibleUsages
                );

            }
        );
        this.setThrowableResolver(
            NoHelpException.class,
            (exception, imperat, context) -> {
                Command<S> cmdUsed;
                if (context instanceof ResolvedContext<S> resolvedContext) {
                    cmdUsed = resolvedContext.getLastUsedCommand();
                } else {
                    cmdUsed = context.command();
                }
                assert cmdUsed != null;
                context.source().error("No Help available for '<command>'".replace("<command>", cmdUsed.name()));
            }
        );
        this.setThrowableResolver(
            NoHelpPageException.class,
            (exception, imperat, context) -> {
                if (!(context instanceof ResolvedContext<S> resolvedContext) || resolvedContext.getDetectedUsage() == null
                    || resolvedContext.getDetectedUsage().isHelp()) {
                    throw new IllegalCallerException("Called NoHelpPageCaption in wrong the wrong sequence/part of the code");
                }

                int page = resolvedContext.getArgumentOr("page", 1);
                context.source().error("Page '<page>' doesn't exist!".replace("<page>", String.valueOf(page)));
            }
        );
    }


    @Override
    public String commandPrefix() {
        return commandPrefix;
    }

    @Override
    public void setCommandPrefix(String cmdPrefix) {
        this.commandPrefix = cmdPrefix;
    }

    /**
     * Sets the whole pre-processing chain
     *
     * @param chain the chain to set
     */
    @Override
    public void setPreProcessorsChain(CommandProcessingChain<S, CommandPreProcessor<S>> chain) {
        Preconditions.notNull(chain, "pre-processors chain");
        this.globalPreProcessors = chain;
    }

    /**
     * Sets the whole post-processing chain
     *
     * @param chain the chain to set
     */
    @Override
    public void setPostProcessorsChain(CommandProcessingChain<S, CommandPostProcessor<S>> chain) {
        Preconditions.notNull(chain, "post-processors chain");
        this.globalPostProcessors = chain;
    }

    /**
     * @return gets the pre-processors in the chain of execution
     * @see CommandPreProcessor
     */
    @Override
    public CommandProcessingChain<S, CommandPreProcessor<S>> getPreProcessors() {
        return globalPreProcessors;
    }

    /**
     * @return gets the post-processors in the chain of execution
     * @see CommandPostProcessor
     */
    @Override
    public CommandProcessingChain<S, CommandPostProcessor<S>> getPostProcessors() {
        return globalPostProcessors;
    }

    /**
     * @return {@link PermissionResolver} for the dispatcher
     */
    @Override
    public @NotNull PermissionResolver<S> getPermissionResolver() {
        return permissionResolver;
    }

    /**
     * Sets the permission resolver for the platform
     *
     * @param permissionResolver the permission resolver to set
     */
    @Override
    public void setPermissionResolver(@NotNull PermissionResolver<S> permissionResolver) {
        this.permissionResolver = permissionResolver;
    }

    /**
     * @return the factory for creation of
     * command related contexts {@link Context}
     */
    @Override
    public @NotNull ContextFactory<S> getContextFactory() {
        return contextFactory;
    }


    /**
     * sets the context factory {@link ContextFactory} for the contexts
     *
     * @param contextFactory the context factory to set
     */
    @Override
    public void setContextFactory(@NotNull ContextFactory<S> contextFactory) {
        this.contextFactory = contextFactory;
    }

    /**
     * Checks whether the valueType has
     * a registered context-resolver
     *
     * @param type the valueType
     * @return whether the valueType has
     * a context-resolver
     */
    @Override
    public boolean hasContextResolver(Type type) {
        return getContextResolver(type) != null;
    }

    /**
     * Registers a context resolver factory
     *
     * @param factory the factory to register
     */
    @Override
    public <T> void registerContextResolverFactory(Type type, ContextResolverFactory<S, T> factory) {
        contextResolverRegistry.registerFactory(type, factory);
    }

    /**
     * @return returns the factory for creation of
     * {@link ContextResolver}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable ContextResolverFactory<S, T> getContextResolverFactory(Type type) {
        return (ContextResolverFactory<S, T>) contextResolverRegistry.getFactoryFor(type).orElse(null);
    }

    /**
     * Fetches {@link ContextResolver} for a certain valueType
     *
     * @param resolvingContextType the valueType for this resolver
     * @return the context resolver
     */
    @Override
    public <T> @Nullable ContextResolver<S, T> getContextResolver(Type resolvingContextType) {
        return contextResolverRegistry.getResolverWithoutParameterElement(resolvingContextType);
    }

    /**
     * Fetches the context resolver for {@link ParameterElement} of a method
     *
     * @param element the element
     * @return the {@link ContextResolver} for this element
     */
    @Override
    public <T> @Nullable ContextResolver<S, T> getMethodParamContextResolver(@NotNull ParameterElement element) {
        Preconditions.notNull(element, "element");
        return contextResolverRegistry.getContextResolver(element.getType(), element);
    }

    /**
     * Registers {@link ContextResolver}
     *
     * @param type     the class-valueType of value being resolved from context
     * @param resolver the resolver for this value
     */
    @Override
    public <T> void registerContextResolver(Type type,
                                            @NotNull ContextResolver<S, T> resolver) {
        contextResolverRegistry.registerResolver(type, resolver);
    }

    /**
     * Registers {@link dev.velix.imperat.command.parameters.type.ParameterType}
     *
     * @param type     the class-valueType of value being resolved from context
     * @param resolver the resolver for this value
     */
    @Override
    public <T> void registerParamType(Class<T> type, @NotNull ParameterType<S, T> resolver) {
        Preconditions.notNull(type, "type");
        Preconditions.notNull(resolver, "resolver");
        paramTypeRegistry.registerResolver(type, ()-> resolver);
        paramTypeRegistry.registerArrayInitializer(type, (length) -> (Object[]) Array.newInstance(type, length));
    }

    /**
     * Registers a supplier function that provides new instances of a specific Collection type.
     * This allows the framework to create appropriate collection instances during deserialization
     * or initialization processes.
     *
     * @param collectionType      the Class object representing the collection type
     * @param newInstanceSupplier a Supplier that creates new instances of the collection type
     * @throws NullPointerException     if collectionType or newInstanceSupplier is null
     */
    @Override
    public <C extends Collection<?>> void registerCollectionInitializer(Class<C> collectionType, Supplier<C> newInstanceSupplier) {
        Preconditions.notNull(collectionType, "collectionType");
        Preconditions.notNull(newInstanceSupplier, "newInstanceSupplier");
        paramTypeRegistry.registerCollectionInitializer(collectionType, newInstanceSupplier);
    }

    /**
     * Registers a supplier function that provides new instances of a specific Map type.
     * This allows the framework to create appropriate map instances during deserialization
     * or initialization processes.
     *
     * @param mapType             the Class object representing the map type
     * @param newInstanceSupplier a Supplier that creates new instances of the map type
     * @throws NullPointerException     if mapType or newInstanceSupplier is null
     */
    @Override
    public <M extends Map<?, ?>> void registerMapInitializer(Class<M> mapType, Supplier<M> newInstanceSupplier) {
        Preconditions.notNull(mapType, "mapType");
        Preconditions.notNull(newInstanceSupplier, "newInstanceSupplier");
        paramTypeRegistry.registerMapInitializer(mapType, newInstanceSupplier);
    }


    /**
     * Fetches {@link ParameterType} for a certain value
     *
     * @param resolvingValueType the value that the resolver ends providing it from the context
     * @return the context resolver of a certain valueType
     */
    @Override
    public @Nullable ParameterType<S, ?> getParameterType(Type resolvingValueType) {
        return paramTypeRegistry.getResolver(resolvingValueType).orElse(null);
    }

    /**
     * Checks whether the command tree operates in strict mode.
     * <p>
     * In strict mode, the command tree may enforce additional validation rules (syntax checks) during command context validation.
     * strict mode ensures that when the input is executed.
     * the command tree doesn't consider type differences in usage-parameter lookups.
     * </p>
     *
     * @return {@code true} if the command tree is in strict mode, {@code false} otherwise.
     * @see #setStrictCommandTree(boolean)
     */
    @Override
    public boolean strictCommandTree() {
        return strictCommandTree;
    }

    /**
     * Enables or disables strict mode for the command tree.
     * <p>
     * When enabled ({@code strict = true}), the command tree may perform rigorous validation
     * checks. Disabling strict mode ({@code strict = false}) may allow for more flexible command
     * usages parsing.
     * </p>
     *
     * @param strict {@code true} to enable strict mode, {@code false} to disable it.
     * @throws UnsupportedOperationException If the strict mode cannot be changed at runtime
     *                                       (e.g., after the command tree has been finalized or locked).
     * @see #strictCommandTree()
     */
    @Override
    public void setStrictCommandTree(boolean strict) {
        this.strictCommandTree = strict;
    }

    /**
     * Fetches the suggestion provider/resolver for a specific valueType of
     * argument or parameter.
     *
     * @param type the valueType
     * @return the {@link SuggestionResolver} instance for that valueType
     */
    @Override
    public @Nullable SuggestionResolver<S> getSuggestionResolverByType(Type type) {
        return paramTypeRegistry.getResolver(type)
            .map(ParameterType::getSuggestionResolver)
            .orElse(null);
    }


    /**
     * Fetches the suggestion provider/resolver for a specific argument
     *
     * @param name the name of the argument
     * @return the {@link SuggestionResolver} instance for that argument
     */
    public @Nullable SuggestionResolver<S> getNamedSuggestionResolver(String name) {
        return suggestionResolverRegistry.getResolverByName(name);
    }

    /**
     * Registers a suggestion resolver linked
     * directly to a unique name
     *
     * @param name               the unique name/id of the suggestion resolver
     * @param suggestionResolver the suggestion resolver to register
     */
    @Override
    public void registerNamedSuggestionResolver(String name, SuggestionResolver<S> suggestionResolver) {
        suggestionResolverRegistry.registerNamedResolver(name.toLowerCase(), suggestionResolver);
    }

    /**
     * Registers a placeholder
     *
     * @param placeholder to register
     */
    @Override
    public void registerPlaceholder(Placeholder<S> placeholder) {
        placeholderRegistry.setData(placeholder.id(), placeholder);
    }

    /**
     * The id/format of this placeholder, must be unique and lowercase
     *
     * @param id the id for the placeholder
     * @return the placeholder
     */
    @Override
    public Optional<Placeholder<S>> getPlaceHolder(String id) {
        return placeholderRegistry.getData(id);
    }

    /**
     * Replaces the placeholders of input by their {@link PlaceholderResolver}
     *
     * @param input the input
     * @return the processed/replaced text input.
     */
    @Override
    public @NotNull String replacePlaceholders(String input) {
        return placeholderRegistry.resolvedString(input);
    }

    /**
     * Replaces the placeholders on each string of the array,
     * modifying the input array content.
     *
     * @param array the array to replace its string contents
     * @return The placeholder replaced String array
     */
    @Override
    public @NotNull String[] replacePlaceholders(String[] array) {
        return placeholderRegistry.resolvedArray(array);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <R> SourceResolver<S, R> getSourceResolver(Type type) {
        return (SourceResolver<S, R>) sourceResolverRegistry.getData(type).orElse(null);
    }

    @Override
    public <R> void registerSourceResolver(Type type, SourceResolver<S, R> sourceResolver) {
        sourceResolverRegistry.setData(type, sourceResolver);
    }

    /**
     * Sets the usage verifier to a new instance
     *
     * @param usageVerifier the usage verifier to set
     */
    @Override
    public void setUsageVerifier(UsageVerifier<S> usageVerifier) {
        this.verifier = usageVerifier;
    }

    /**
     * Registers the dependency to the type
     *
     * @param type     the type for the dependency
     * @param resolver the resolver
     */
    @Override
    public void registerDependencyResolver(Type type, DependencySupplier resolver) {
        this.dependencyResolverRegistry.setData(type, resolver);
    }


    /**
     * Resolves dependency of certain type
     *
     * @param type the type
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T resolveDependency(Type type) {
        return (T) dependencyResolverRegistry.getData(type)
            .map(DependencySupplier::get).orElse(null);
    }

    /**
     * @return the usage verifier
     */
    @Override
    public UsageVerifier<S> getUsageVerifier() {
        return verifier;
    }

    /**
     * @return The template for showing help
     */
    @Override
    public @Nullable HelpProvider<S> getHelpProvider() {
        return provider;
    }

    /**
     * Set the help template to use
     *
     * @param template the help template
     */
    @Override
    public void setHelpProvider(@Nullable HelpProvider<S> template) {
        this.provider = template;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Throwable> ThrowableResolver<T, S> getThrowableResolver(Class<T> exception) {
        Class<?> current = exception;
        while (current != null && Throwable.class.isAssignableFrom(current)) {
            var resolver = handlers.get(current);
            if (resolver != null) {
                return (ThrowableResolver<T, S>) resolver;
            }
            current = current.getSuperclass();
        }
        return null;
    }

    @Override
    public <T extends Throwable> void setThrowableResolver(Class<T> exception, ThrowableResolver<T, S> handler) {
        this.handlers.put(exception, handler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleExecutionThrowable(
        @NotNull final Throwable throwable,
        final Context<S> context,
        final Class<?> owning,
        final String methodName
    ) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof SelfHandledException selfHandledException) {
                selfHandledException.handle(this, context);
                return;
            }

            ThrowableResolver<? super Throwable, S> handler = (ThrowableResolver<? super Throwable, S>) this.getThrowableResolver(current.getClass());
            if (handler != null) {
                handler.resolve(current, this, context);
                return;
            }

            current = current.getCause();
        }

        ImperatDebugger.error(owning, methodName, throwable);
    }


}
