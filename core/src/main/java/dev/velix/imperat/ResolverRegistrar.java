package dev.velix.imperat;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.ContextResolverFactory;
import dev.velix.imperat.resolvers.ContextResolver;
import dev.velix.imperat.resolvers.PermissionResolver;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.resolvers.ValueResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Collection;

public sealed interface ResolverRegistrar<S extends Source> permits Imperat {
    
    /**
     * @return {@link PermissionResolver} for the dispatcher
     */
    PermissionResolver<S> getPermissionResolver();
    
    
    /**
     * Registers a context resolver factory
     *
     * @param factory the factory to register
     */
    void registerContextResolverFactory(ContextResolverFactory<S> factory);
    
    /**
     * Checks whether the type has
     * a registered context-resolver
     *
     * @param type the type
     * @return whether the type has
     * a context-resolver
     */
    boolean hasContextResolver(Type type);
    
    /**
     * @return returns the factory for creation of
     * {@link ContextResolver}
     */
    ContextResolverFactory<S> getContextResolverFactory();
    
    /**
     * Fetches {@link ContextResolver} for a certain type
     *
     * @param resolvingContextType the type for this resolver
     * @param <T>                  the type of class
     * @return the context resolver
     */
    <T> ContextResolver<S, T> getContextResolver(Type resolvingContextType);
    
    /**
     * Fetches the {@link ContextResolver} suitable for the {@link CommandParameter}
     *
     * @param commandParameter the parameter of a command's usage
     * @param <T>              the type of value that will be resolved by {@link ValueResolver}
     * @return the context resolver for this parameter's value type
     */
    default <T> ContextResolver<S, T> getContextResolver(CommandParameter commandParameter) {
        return getContextResolver(commandParameter.getType());
    }
    
    /**
     * Registers {@link ContextResolver}
     *
     * @param type     the class-type of value being resolved from context
     * @param resolver the resolver for this value
     * @param <T>      the type of value being resolved from context
     */
    <T>
    void registerContextResolver(Type type, @NotNull ContextResolver<S, T> resolver);
    
    /**
     * Fetches {@link ValueResolver} for a certain value
     *
     * @param resolvingValueType the value that the resolver ends providing it from the context
     * @param <T>                the type of value resolved from the context resolver
     * @return the value resolver of a certain type
     */
    @Nullable
    <T> ValueResolver<S, T> getValueResolver(Type resolvingValueType);
    
    /**
     * Fetches the {@link ValueResolver} suitable for the {@link CommandParameter}
     *
     * @param commandParameter the parameter of a command's usage
     * @param <T>              the type of value that will be resolved by {@link ValueResolver}
     * @return the value resolver for this parameter's value type
     */
    default <T> ValueResolver<S, T> getValueResolver(CommandParameter commandParameter) {
        return getValueResolver(commandParameter.getType());
    }
    
    /**
     * Registers {@link ValueResolver}
     *
     * @param type     the class-type of value being resolved from context
     * @param resolver the resolver for this value
     * @param <T>      the type of value being resolved from context
     */
    <T> void registerValueResolver(Type type, @NotNull ValueResolver<S, T> resolver);
    
    /**
     * @return all currently registered {@link ValueResolver}
     */
    Collection<? extends ValueResolver<S, ?>> getRegisteredValueResolvers();
    
    /**
     * Fetches the suggestion provider/resolver for a specific type of
     * argument or parameter.
     *
     * @param parameter the parameter symbolizing the type and argument name
     * @param <T>       the type parameter representing the type of value that the suggestion resolver
     *                  will work with
     * @return the {@link SuggestionResolver} instance for that type
     */
    @SuppressWarnings("unchecked")
    default @Nullable <T> SuggestionResolver<S, T> getParameterSuggestionResolver(CommandParameter parameter) {
        SuggestionResolver<S, T> parameterSpecificResolver = parameter.getSuggestionResolver();
        if (parameterSpecificResolver == null)
            return getSuggestionResolverByType((Class<T>) parameter.getType());
        else
            return parameterSpecificResolver;
    }
    
    /**
     * Fetches the suggestion provider/resolver for a specific type of
     * argument or parameter.
     *
     * @param clazz the clazz symbolizing the type
     * @param <T>   the type parameter representing the type of value that the suggestion resolver
     *              will work with
     * @return the {@link SuggestionResolver} instance for that type
     */
    @Nullable
    <T> SuggestionResolver<S, T> getSuggestionResolverByType(Class<T> clazz);
    
    /**
     * Fetches the suggestion provider/resolver that is registered by its unique name
     *
     * @param name the name of the argument
     * @param <T>  the type parameter representing the type of value that the suggestion resolver
     *             will work with
     * @return the {@link SuggestionResolver} instance for that argument
     */
    @Nullable
    <T> SuggestionResolver<S, T> getNamedSuggestionResolver(String name);
    
    /**
     * Registers a suggestion resolver
     *
     * @param suggestionResolver the suggestion resolver to register
     * @param <T>                the type of value that the suggestion resolver will work with.
     */
    <T> void registerSuggestionResolver(SuggestionResolver<S, T> suggestionResolver);
    
    /**
     * Registers a suggestion resolver
     *
     * @param name               the name of the suggestion resolver
     * @param suggestionResolver the suggestion resolver to register
     * @param <T>                the type of value that the suggestion resolver will work with.
     */
    <T> void registerNamedSuggestionResolver(String name, SuggestionResolver<S, T> suggestionResolver);
    
    
}
