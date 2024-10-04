package dev.velix.imperat;

import dev.velix.imperat.annotations.base.element.ParameterElement;
import dev.velix.imperat.command.ContextResolverFactory;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.placeholders.Placeholder;
import dev.velix.imperat.placeholders.PlaceholderResolver;
import dev.velix.imperat.resolvers.*;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

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
    void registerContextResolverFactory(Type type, ContextResolverFactory<S> factory);

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
     * @param resolvingContextType the type the factory is registered to
     * @return returns the factory for creation of
     * {@link ContextResolver}
     */
    @Nullable
    ContextResolverFactory<S> getContextResolverFactory(Type resolvingContextType);

    /**
     * Fetches {@link ContextResolver} for a certain type
     *
     * @param resolvingContextType the type for this resolver
     * @param <T>                  the type of class
     * @return the context resolver
     */
    @Nullable
    <T> ContextResolver<S, T> getContextResolver(Type resolvingContextType);

    /**
     * Fetches the context resolver for {@link ParameterElement} of a method
     *
     * @param element the element
     * @param <T>     the type of value this parameter should be resolved into
     * @return the {@link ContextResolver} for this element
     */
    @Nullable
    <T> ContextResolver<S, T> getMethodParamContextResolver(@NotNull ParameterElement element);

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
    default @NotNull SuggestionResolver<S> getParameterSuggestionResolver(CommandParameter<S> parameter) {
        SuggestionResolver<S> parameterSpecificResolver = parameter.getSuggestionResolver();
        if (parameterSpecificResolver == null) {
            var resolverByType = getSuggestionResolverByType(parameter.type());
            if (resolverByType != null) return resolverByType;
            else return SuggestionResolver.plain(Collections.singletonList(parameter.format()));
        } else
            return parameterSpecificResolver;
    }

    /**
     * Fetches the suggestion provider/resolver for a specific type of
     * argument or parameter.
     *
     * @param type the type
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

    /**
     * Fetches the {@link SourceResolver} from an internal registry.
     *
     * @param type the target source type
     * @param <R>  the new source type parameter
     * @return the {@link SourceResolver} for specific type
     */
    <R> @Nullable SourceResolver<S, R> getSourceResolver(Type type);

    /**
     * Registers the {@link SourceResolver} into an internal registry
     *
     * @param type           the target source type
     * @param sourceResolver the source resolver to register
     * @param <R>            the new source type parameter
     */
    default <R> void registerSourceResolver(TypeWrap<R> type, SourceResolver<S, R> sourceResolver) {
        registerSourceResolver(type.getType(), sourceResolver);
    }

    /**
     * Registers the {@link SourceResolver} into an internal registry
     *
     * @param type           the target source type
     * @param sourceResolver the source resolver to register
     * @param <R>            the new source type parameter
     */
    <R> void registerSourceResolver(Type type, SourceResolver<S, R> sourceResolver);

    default boolean hasSourceResolver(Type wrap) {
        return getSourceResolver(wrap) != null;
    }

    /**
     * Registers a placeholder
     *
     * @param placeholder to register
     */
    void registerPlaceholder(Placeholder<S> placeholder);

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
}
