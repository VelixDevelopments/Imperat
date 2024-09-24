package dev.velix.imperat;

import dev.velix.imperat.command.ContextResolverFactory;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;

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
    default <T> ContextResolver<S, T> getContextResolver(CommandParameter<S> commandParameter) {
        return getContextResolver(commandParameter.type());
    }
    
    /**
     * Registers {@link ContextResolver}
     *
     * @param type     the class-type of value being resolved from context
     * @param resolver the resolver for this value
     * @param <T>      the type of value being resolved from context
     */
    <T> void registerContextResolver(Type type, @NotNull ContextResolver<S, T> resolver);
    
    /**
     * Fetches {@link ValueResolver} for a certain value
     *
     * @param resolvingValueType the value that the resolver ends providing it from the context
     * @return the value resolver of a certain type
     */
    @Nullable
    ValueResolver<S, ?> getValueResolver(Type resolvingValueType);
    
    /**
     * Fetches the {@link ValueResolver} suitable for the {@link CommandParameter}
     *
     * @param commandParameter the parameter of a command's usage
     * @return the value resolver for this parameter's value type
     */
    default ValueResolver<S, ?> getValueResolver(CommandParameter<S> commandParameter) {
        return getValueResolver(commandParameter.type());
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
     * @return the {@link SuggestionResolver} instance for that type
     */
    @SuppressWarnings("uncecked")
    default @Nullable SuggestionResolver<S> getParameterSuggestionResolver(CommandParameter<S> parameter) {
        SuggestionResolver<S> parameterSpecificResolver = parameter.getSuggestionResolver();
        if (parameterSpecificResolver == null) {
            var resolverByType = getSuggestionResolverByType(parameter.type());
            if (resolverByType != null) return resolverByType;
            else return SuggestionResolver.plain(Collections.singletonList(parameter.format()));
        }
        else
            return parameterSpecificResolver;
    }
    
    /**
     * Fetches the suggestion provider/resolver for a specific type of
     * argument or parameter.
     *
     * @param type  the type
     *
     * @return the {@link SuggestionResolver} instance for that type
     */
    @Nullable
    SuggestionResolver<S> getSuggestionResolverByType(Type type);
    
    /**
     * Fetches the suggestion provider/resolver registered by its unique name
     *
     * @param name the name of the argument
     * @return the {@link SuggestionResolver} instance for that argument
     */
    @Nullable
    SuggestionResolver<S> getNamedSuggestionResolver(String name);
    
    /**
     * Registers a suggestion resolver
     *
     * @param suggestionResolver the suggestion resolver to register
     * @param <T>                the type of value that the suggestion resolver will work with.
     */
    <T> void registerSuggestionResolver(TypeSuggestionResolver<S, T> suggestionResolver);
    
    /**
     * Registers a suggestion resolver to a type
     *
     * @param type               the type
     * @param suggestionResolver the suggestion resolver.
     */
    void registerSuggestionResolver(Type type, SuggestionResolver<S> suggestionResolver);
    
    /**
     * Registers a suggestion resolver
     *
     * @param name               the name of the suggestion resolver
     * @param suggestionResolver the suggestion resolver to register
     */
    void registerNamedSuggestionResolver(String name, SuggestionResolver<S> suggestionResolver);
    
}
