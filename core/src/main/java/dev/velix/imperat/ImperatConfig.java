package dev.velix.imperat;

import dev.velix.imperat.annotations.base.AnnotationReplacer;
import dev.velix.imperat.annotations.base.element.ParameterElement;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.ContextResolverFactory;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.context.internal.ContextFactory;
import dev.velix.imperat.exception.ThrowableResolver;
import dev.velix.imperat.placeholders.Placeholder;
import dev.velix.imperat.placeholders.PlaceholderResolver;
import dev.velix.imperat.resolvers.ContextResolver;
import dev.velix.imperat.resolvers.DependencySupplier;
import dev.velix.imperat.resolvers.PermissionResolver;
import dev.velix.imperat.verification.UsageVerifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * The {@code ImperatConfig} interface defines the core configuration and interaction points
 * for a command processing system. It provides methods for registering handlers,
 * resolving dependencies, and managing the execution context. This interface enables
 * integration of custom resolvers, context factories, permission management, and
 * placeholder handling.
 * <p>
 * Implementations of this interface are responsible for providing these required
 * functionalities and may enforce strict validation and processing rules based on
 * the specified configurations.
 * </p>
 *
 * @param <S> The type of the source object used by the configuration, which implements the {@link Source} interface.
 */
public sealed interface ImperatConfig<S extends Source> extends
    ProcessorRegistrar<S>, ResolverRegistrar<S>,
    CommandHelpHandler<S>, ThrowableHandler<S>
    permits ImperatConfigImpl {

    /**
     * @return The command prefix
     */
    String commandPrefix();

    void setCommandPrefix(String cmdPrefix);


    /**
     * Fetches {@link ParameterType} for a certain value
     *
     * @param resolvingValueType the value that the resolver ends providing it from the context
     * @return the value resolver of a certain valueType
     */
    @Nullable
    ParameterType<S, ?> getParameterType(Type resolvingValueType);

    default boolean hasParameterType(Type type) {
        return getParameterType(type) != null;
    }
    
    <A extends Annotation> void registerAnnotationReplacer(Class<A> type, AnnotationReplacer<A> replacer);
    
    <A extends Annotation> void applyAnnotationReplacers(Imperat<S> imperat);
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
     *         {@code false} if only one optional parameter should be suggested at a time
     * @see #setOptionalParameterSuggestionOverlap(boolean)
     */
    boolean isOptionalParameterSuggestionOverlappingEnabled();
    
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
    void setOptionalParameterSuggestionOverlap(boolean enabled);
    
    
    /**
     * Checks whether the command tree operates in strict mode.
     * <p>
     * In strict mode, the command tree may enforce additional validation rules (syntax checks) during command context validation.
     * strict mode ensures that when the input is executed.
     * the command tree doesn't consider type differences in usage-parameter lookups.
     * </p>
     *
     * @return {@code true} if the command tree is in strict mode, {@code false} otherwise.
     *
     * @see #setStrictCommandTree(boolean)
     */
    boolean strictCommandTree();

    /**
     * Enables or disables strict mode for the command tree.
     * <p>
     * When enabled ({@code strict = true}), the command tree may perform rigorous validation
     * checks. Disabling strict mode ({@code strict = false}) may allow for more flexible command
     * usages parsing.
     * </p>
     *
     * @param strict {@code true} to enable strict mode, {@code false} to disable it.
     *
     * @throws UnsupportedOperationException If the strict mode cannot be changed at runtime
     *         (e.g., after the command tree has been finalized or locked).
     *
     * @see #strictCommandTree()
     */
    void setStrictCommandTree(boolean strict);

    /**
     * Checks whether the valueType has
     * a registered context-resolver
     *
     * @param type the valueType
     * @return whether the valueType has
     * a context-resolver
     */
    boolean hasContextResolver(Type type);

    /**
     * Fetches {@link ContextResolver} for a certain valueType
     *
     * @param resolvingContextType the valueType for this resolver
     * @param <T>                  the valueType of class
     * @return the context resolver
     */
    @Nullable
    <T> ContextResolver<S, T> getContextResolver(Type resolvingContextType);

    /**
     * Fetches the context resolver for {@link ParameterElement} of a method
     *
     * @param element the element
     * @param <T>     the valueType of value this parameter should be resolved into
     * @return the {@link ContextResolver} for this element
     */
    @Nullable
    <T> ContextResolver<S, T> getMethodParamContextResolver(@NotNull ParameterElement element);

    /**
     * Fetches the {@link ContextResolver} suitable for the {@link CommandParameter}
     *
     * @param commandParameter the parameter of a command's usage
     * @param <T>              the valueType of value that will be resolved by {@link ParameterType#resolve(ExecutionContext, CommandInputStream, String)}
     * @return the context resolver for this parameter's value valueType
     */
    default <T> ContextResolver<S, T> getContextResolver(CommandParameter<S> commandParameter) {
        return getContextResolver(commandParameter.valueType());
    }

    /**
     * @param resolvingContextType the valueType the factory is registered to
     * @return returns the factory for creation of
     * {@link ContextResolver}
     */
    @Nullable
    <T> ContextResolverFactory<S, T> getContextResolverFactory(Type resolvingContextType);

    /**
     * @return {@link PermissionResolver} for the dispatcher
     */
    PermissionResolver<S> getPermissionResolver();

    /**
     * Sets the permission resolver for the platform
     *
     * @param permissionResolver the permission resolver to set
     */
    void setPermissionResolver(PermissionResolver<S> permissionResolver);

    /**
     * @return the factory for creation of
     * command related contexts {@link Context}
     */
    ContextFactory<S> getContextFactory();

    /**
     * The id/format of this placeholder, must be unique and lowercase
     *
     * @param id the id for the placeholder
     * @return the placeholder
     */
    Optional<Placeholder<S>> getPlaceHolder(String id);

    /**
     * Replaces the placeholders of input by their {@link PlaceholderResolver}
     *
     * @param input the input
     * @return the processed/replaced text input.
     */
    @NotNull
    String replacePlaceholders(String input);

    /**
     * Replaces the placeholders on each string of the array,
     * modifying the input array content.
     *
     * @param array the array to replace its string contents
     * @return The placeholder replaced String array
     */
    @NotNull
    String[] replacePlaceholders(String[] array);

    /**
     * sets the context factory {@link ContextFactory} for the contexts
     *
     * @param contextFactory the context factory to set
     */
    void setContextFactory(ContextFactory<S> contextFactory);

    /**
     * Registers the dependency to the type
     *
     * @param type     the type for the dependency
     * @param resolver the resolver
     */
    void registerDependencyResolver(Type type, DependencySupplier resolver);

    /**
     * Resolves dependency of certain type
     *
     * @param type the type
     */
    <T> @Nullable T resolveDependency(Type type);


    /**
     * @return the usage verifier
     */
    UsageVerifier<S> getUsageVerifier();

    /**
     * Sets the usage verifier to a new instance
     *
     * @param usageVerifier the usage verifier to set
     */
    void setUsageVerifier(UsageVerifier<S> usageVerifier);

    default boolean hasSourceResolver(Type wrap) {
        return getSourceResolver(wrap) != null;
    }

    /**
     * Registers a new {@link ThrowableResolver} for the specified valueType of throwable.
     * This allows customizing the handling of specific throwable types within the application.
     *
     * @param exception The class of the throwable to set the resolver for.
     * @param handler   The {@link ThrowableResolver} to be registered for the specified throwable valueType.
     * @param <T>       The valueType of the throwable.
     */
    <T extends Throwable> void setThrowableResolver(
        final Class<T> exception,
        final ThrowableResolver<T, S> handler
    );
    
    @NotNull CommandUsage.Builder<S> getGlobalDefaultUsage();
    
    void setGlobalDefaultUsage(@NotNull CommandUsage.Builder<S> globalDefaultUsage);
}
