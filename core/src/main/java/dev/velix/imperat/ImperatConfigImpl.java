package dev.velix.imperat;

import dev.velix.imperat.annotations.base.AnnotationReplacer;
import dev.velix.imperat.annotations.base.element.ParameterElement;
import dev.velix.imperat.command.*;
import dev.velix.imperat.command.parameters.NumericRange;
import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.command.processors.CommandProcessingChain;
import dev.velix.imperat.command.processors.impl.DefaultProcessors;
import dev.velix.imperat.command.returns.ReturnResolver;
import dev.velix.imperat.command.suggestions.SuggestionResolverRegistry;
import dev.velix.imperat.command.tree.ClosestUsageSearch;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ParamTypeRegistry;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.ContextFactory;
import dev.velix.imperat.exception.CooldownException;
import dev.velix.imperat.exception.InvalidSourceException;
import dev.velix.imperat.exception.InvalidSyntaxException;
import dev.velix.imperat.exception.InvalidUUIDException;
import dev.velix.imperat.exception.MissingFlagInputException;
import dev.velix.imperat.exception.NoHelpException;
import dev.velix.imperat.exception.NoHelpPageException;
import dev.velix.imperat.exception.NumberOutOfRangeException;
import dev.velix.imperat.exception.OnlyPlayerAllowedException;
import dev.velix.imperat.exception.PermissionDeniedException;
import dev.velix.imperat.exception.SelfHandledException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.exception.ThrowableResolver;
import dev.velix.imperat.exception.UnknownFlagException;
import dev.velix.imperat.exception.parse.InvalidBooleanException;
import dev.velix.imperat.exception.parse.InvalidEnumException;
import dev.velix.imperat.exception.parse.InvalidNumberFormatException;
import dev.velix.imperat.exception.parse.UnknownSubCommandException;
import dev.velix.imperat.exception.parse.ValueOutOfConstraintException;
import dev.velix.imperat.exception.parse.InvalidMapEntryFormatException;
import dev.velix.imperat.exception.parse.WordOutOfRestrictionsException;
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
import dev.velix.imperat.util.TypeWrap;
import dev.velix.imperat.verification.UsageVerifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

final class ImperatConfigImpl<S extends Source> implements ImperatConfig<S> {

    private @NotNull SuggestionResolver<S> defaultSuggestionResolver =
            (context, input) ->
            Collections.emptyList();

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
    private final ReturnResolverRegistry<S> returnResolverRegistry;

    private final Map<Class<? extends Throwable>, ThrowableResolver<?, S>> handlers = new HashMap<>();

    private @NotNull CommandProcessingChain<S, CommandPreProcessor<S>> globalPreProcessors;
    private @NotNull CommandProcessingChain<S, CommandPostProcessor<S>> globalPostProcessors;

    private boolean strictCommandTree = false;
    private boolean overlapOptionalParameterSuggestions = false;
    
    private String commandPrefix = "/";

    private final Map<Class<?>, AnnotationReplacer<?>> annotationReplacerMap = new HashMap<>();
    
    private CommandUsage.Builder<S> globalDefaultUsage = CommandUsage.builder();
    
    ImperatConfigImpl() {
        contextResolverRegistry = ContextResolverRegistry.createDefault(this);
        paramTypeRegistry = ParamTypeRegistry.createDefault();
        suggestionResolverRegistry = SuggestionResolverRegistry.createDefault(this);
        sourceResolverRegistry = SourceResolverRegistry.createDefault();
        returnResolverRegistry = ReturnResolverRegistry.createDefault();
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

        this.setThrowableResolver(UnknownFlagException.class,(ex, imperat, context)-> {
            context.source().error("Unknown flag '" + ex.getInput() + "'");
        });

        this.setThrowableResolver(MissingFlagInputException.class,(ex, imperat, context)-> {
            context.source().error("Please enter the value for flag '" + ex.getInput() + "'");
        });

        this.setThrowableResolver(OnlyPlayerAllowedException.class, (ex, imperat, context)-> {
            context.source().error("Only players can do this!");
        });

        this.setThrowableResolver(ValueOutOfConstraintException.class, (ex, imperat, context)-> {
            context.source().error("Input '" + ex.getInput() + "' is not one of: [" + String.join(",",  ex.getAllowedValues()) + "]");
        });

        this.setThrowableResolver(WordOutOfRestrictionsException.class, (ex, imperat, context)-> {
            context.source().error("Word '" + ex.getInput() + "' is not within the given restrictions=" + String.join(",",ex.getRestrictions()));
        });

        this.setThrowableResolver(UnknownSubCommandException.class, (exception, imperat, context) -> {
            context.source().error("Unknown sub-command '" + exception.getInput() + "'");
        });

        this.setThrowableResolver(InvalidMapEntryFormatException.class, (exception, imperat, context) -> {
            InvalidMapEntryFormatException.Reason reason = exception.getReason();
            String extraMsg = "";
            if(reason == InvalidMapEntryFormatException.Reason.MISSING_SEPARATOR) {
                extraMsg = "entry doesn't contain '" + exception.getRequiredSeparator() + "'";
            }else if(reason == InvalidMapEntryFormatException.Reason.NOT_TWO_ELEMENTS) {
                extraMsg = "entry is not made of 2 elements";
            }
            context.source().error("Invalid map entry '" + exception.getInput() + "'" + (!extraMsg.isEmpty() ? ", " + extraMsg : ""));
        });

        this.setThrowableResolver(InvalidBooleanException.class, (exception, imperat, context) -> {
            context.source().error("Invalid boolean '" + exception.getInput() + "'");
        });

        this.setThrowableResolver(InvalidEnumException.class, (exception, imperat, context) -> {
            context.source().error("Invalid " + exception.getEnumType().getTypeName() + " '" + exception.getInput() + "'");
        });

        this.setThrowableResolver(InvalidNumberFormatException.class, (exception, imperat, context) -> {
            context.source().error("Invalid " + exception.getNumberTypeDisplay() + " format '" + exception.getInput() + "'");
        });

        this.setThrowableResolver(NumberOutOfRangeException.class, ((exception, imperat, context) -> {
            NumericRange range = exception.getRange();
            final StringBuilder builder = new StringBuilder();
            if (range.getMin() != Double.MIN_VALUE && range.getMax() != Double.MAX_VALUE)
                builder.append("within ").append(range.getMin()).append('-').append(range.getMax());
            else if (range.getMin() != Double.MIN_VALUE)
                builder.append("at least '").append(range.getMin()).append("'");
            else if (range.getMax() != Double.MAX_VALUE)
                builder.append("at most '").append(range.getMax()).append("'");
            else
                builder.append("(Open range)");

            String rangeFormatted = builder.toString();
            context.source().error("Value '" + exception.getValue() + "' entered for parameter '" + exception.getParameter().format() + "' must be " + rangeFormatted);
        }));

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
                context.source().error("Invalid uuid-format '" + exception.getInput() + "'")
        );

        this.setThrowableResolver(
            CooldownException.class,
            (exception, imperat, context) -> {
                context.source().error(
                    "Please wait %d second(s) to execute this command again!".formatted(exception.getRemainingDuration().toSeconds())
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
                ClosestUsageSearch<S> closestUsageSearch = context.command().tree().getClosestUsages(context);
                source.error("Invalid command usage '/" + context.command().name() + " " + context.arguments().join(" ") + "'");
                source.error("Closest Command Usage: " + (imperat.commandPrefix() + CommandUsage.format(context.label(), closestUsageSearch.getClosest())) );
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
                if (!(context instanceof ResolvedContext<S> resolvedContext) || resolvedContext.getDetectedUsage() == null) {
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
    public <T> void registerContextResolver(Type type, @NotNull ContextResolver<S, T> resolver) {
        contextResolverRegistry.registerResolver(type, resolver);
    }

    /**
     * Registers {@link dev.velix.imperat.command.parameters.type.ParameterType}
     *
     * @param type     the class-valueType of value being resolved from context
     * @param resolver the resolver for this value
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> void registerParamType(Type type, @NotNull ParameterType<S, T> resolver) {
        Preconditions.notNull(type, "type");
        Preconditions.notNull(resolver, "resolver");
        paramTypeRegistry.registerResolver(type, ()-> resolver);

        Class<T> rawType = (Class<T>) TypeWrap.of(type).getRawType();
        paramTypeRegistry.registerArrayInitializer(rawType, (length) -> (Object[]) Array.newInstance(rawType, length));
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
     * Retrieves the default suggestion resolver associated with this registrar.
     *
     * @return the {@link SuggestionResolver} instance used as the default resolver
     */
    @Override
    public @NotNull SuggestionResolver<S> getDefaultSuggestionResolver() {
        return defaultSuggestionResolver;
    }

    /**
     * Sets the default suggestion resolver to be used when no specific
     * suggestion resolver is provided. The default suggestion resolver
     * handles the auto-completion of arguments/parameters for commands.
     *
     * @param defaultSuggestionResolver the {@link SuggestionResolver} to be set as default
     */
    @Override
    public void setDefaultSuggestionResolver(@NotNull SuggestionResolver<S> defaultSuggestionResolver) {
        this.defaultSuggestionResolver = defaultSuggestionResolver;
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
    
    @Override
    public <A extends Annotation> void registerAnnotationReplacer(Class<A> type, AnnotationReplacer<A> replacer) {
        annotationReplacerMap.put(type, replacer);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> void applyAnnotationReplacers(Imperat<S> imperat) {
        this.annotationReplacerMap.forEach((type, replacer)-> {
            Class<A> annType = (Class<A>)type;
            AnnotationReplacer<A> annReplacer = (AnnotationReplacer<A>)replacer;
            imperat.registerAnnotationReplacer(annType, annReplacer);
        });
    }
    
    /**
     * Determines whether multiple optional parameters can be suggested simultaneously
     * during tab completion at the same command depth level.
     *
     * <p>When enabled ({@code true}), all available optional parameters will be included
     * in tab completion suggestions, allowing users to see all possible optional arguments
     * they can provide at the current position.
     *
     * <p>When disabled ({@code false}), only the first optional parameter (typically based
     * on priority or registration order) will be suggested, preventing overwhelming users
     * with too many optional choices and reducing ambiguity in command completion.
     *
     * <p>This setting does not affect:
     * <ul>
     *   <li>Required parameters - they are always suggested</li>
     *   <li>Command structure - the actual command tree remains unchanged</li>
     *   <li>Parameter validation - all parameters remain functionally available</li>
     * </ul>
     *
     * @return {@code true} if multiple optional parameters can overlap in suggestions,
     * {@code false} if only one optional parameter should be suggested at a time
     * @see #setOptionalParameterSuggestionOverlap(boolean)
     */
    @Override
    public boolean isOptionalParameterSuggestionOverlappingEnabled() {
        return overlapOptionalParameterSuggestions;
    }
    
    /**
     * Sets whether multiple optional parameters can be suggested simultaneously
     * during tab completion at the same command depth level.
     *
     * <p>This is a configuration setting that affects the behavior of tab completion
     * suggestions without modifying the underlying command structure. The command
     * tree and parameter validation remain unchanged regardless of this setting.
     *
     * <p><strong>Examples:</strong>
     * <pre>{@code
     * // Command structure: /command [count] [extra]
     * //                              \[extra]
     *
     * // When enabled (true):
     * /command <TAB> → shows: [count], [extra]
     *
     * // When disabled (false):
     * /command <TAB> → shows: [count] (first optional only)
     * }</pre>
     *
     * @param enabled {@code true} to allow multiple optional parameter suggestions,
     *                {@code false} to limit to one optional parameter suggestion
     * @see #isOptionalParameterSuggestionOverlappingEnabled()
     */
    @Override
    public void setOptionalParameterSuggestionOverlap(boolean enabled) {
        this.overlapOptionalParameterSuggestions = enabled;
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

    @Override
    public @Nullable <T> ReturnResolver<S, T> getReturnResolver(Type type) {
        return returnResolverRegistry.getReturnResolver(type);
    }

    @Override
    public <T> void registerReturnResolver(Type type, ReturnResolver<S, T> returnResolver) {
        returnResolverRegistry.setData(type, returnResolver);
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
    public CommandUsage.@NotNull Builder<S> getGlobalDefaultUsage() {
        return globalDefaultUsage;
    }
    
    @Override
    public void setGlobalDefaultUsage(CommandUsage.@NotNull Builder<S> globalDefaultUsage) {
        this.globalDefaultUsage = globalDefaultUsage;
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
                ImperatDebugger.debug("Found handler for exception '%s'", current.getClass().getName());
                handler.resolve(current, this, context);
                return;
            }
            else {
                ImperatDebugger.debug("No handler for exception '%s'", current.getClass().getName());
            }

            current = current.getCause();
        }

        ImperatDebugger.error(owning, methodName, throwable);
    }


}
