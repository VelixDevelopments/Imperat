package dev.velix.imperat;

import dev.velix.imperat.annotations.base.element.ParameterElement;
import dev.velix.imperat.command.ContextResolverFactory;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.placeholders.Placeholder;
import dev.velix.imperat.placeholders.PlaceholderResolver;
import dev.velix.imperat.resolvers.ContextResolver;
import dev.velix.imperat.resolvers.PermissionResolver;
import dev.velix.imperat.resolvers.SourceResolver;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
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
     * Checks whether the valueType has
     * a registered context-resolver
     *
     * @param type the valueType
     * @return whether the valueType has
     * a context-resolver
     */
    boolean hasContextResolver(Type type);

    /**
     * @param resolvingContextType the valueType the factory is registered to
     * @return returns the factory for creation of
     * {@link ContextResolver}
     */
    @Nullable
    ContextResolverFactory<S> getContextResolverFactory(Type resolvingContextType);

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
     * @param <T>              the valueType of value that will be resolved by {@link ParameterType#resolve(ExecutionContext, CommandInputStream)}
     * @return the context resolver for this parameter's value valueType
     */
    default <T> ContextResolver<S, T> getContextResolver(CommandParameter<S> commandParameter) {
        return getContextResolver(commandParameter.valueType());
    }

    /**
     * Registers {@link ContextResolver}
     *
     * @param type     the class-valueType of value being resolved from context
     * @param resolver the resolver for this value
     * @param <T>      the valueType of value being resolved from context
     */
    <T> void registerContextResolver(Type type, @NotNull ContextResolver<S, T> resolver);

    /**
     * Fetches {@link ParameterType} for a certain value
     *
     * @param resolvingValueType the value that the resolver ends providing it from the context
     * @return the value resolver of a certain valueType
     */
    @Nullable
    ParameterType<S, ?> getParameterType(Type resolvingValueType);

    /**
     * Registers {@link ParameterType}
     *
     * @param type     the class-valueType of value being resolved from context
     * @param resolver the resolver for this value
     * @param <T>      the valueType of value being resolved from context
     */
    <T> void registerParamType(Type type, @NotNull ParameterType<S, T> resolver);


    /**
     * Fetches the suggestion provider/resolver for a specific valueType of
     * argument or parameter.
     *
     * @param parameter the parameter symbolizing the valueType and argument name
     * @return the {@link SuggestionResolver} instance for that valueType
     */
    @SuppressWarnings("uncecked")
    default @NotNull SuggestionResolver<S> getParameterSuggestionResolver(CommandParameter<S> parameter) {
        SuggestionResolver<S> parameterSpecificResolver = parameter.getSuggestionResolver();
        //ImperatDebugger.debug("Getting the suggestion resolver for param '%s'", parameter.format());
        if (parameterSpecificResolver == null) {
            //ImperatDebugger.debug("Found no specific argument suggestion resolver for param '%s'", parameter.format());
            var resolverByType = getSuggestionResolverByType(parameter.valueType());
            if (resolverByType != null) {
                //ImperatDebugger.debug("Found resolver by type for param '%s'", parameter.format() );
                return resolverByType;
            }
            else return SuggestionResolver.plain(Collections.singletonList(parameter.format()));
        } else
            return parameterSpecificResolver;
    }

    /**
     * Fetches the suggestion provider/resolver for a specific valueType of
     * argument or parameter.
     *
     * @param type the valueType
     * @return the {@link SuggestionResolver} instance for that valueType
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
     * @param name               the name of the suggestion resolver
     * @param suggestionResolver the suggestion resolver to register
     */
    void registerNamedSuggestionResolver(String name, SuggestionResolver<S> suggestionResolver);

    /**
     * Fetches the {@link SourceResolver} from an internal registry.
     *
     * @param type the target source valueType
     * @param <R>  the new source valueType parameter
     * @return the {@link SourceResolver} for specific valueType
     */
    <R> @Nullable SourceResolver<S, R> getSourceResolver(Type type);

    /**
     * Registers the {@link SourceResolver} into an internal registry
     *
     * @param type           the target source valueType
     * @param sourceResolver the source resolver to register
     * @param <R>            the new source valueType parameter
     */
    default <R> void registerSourceResolver(TypeWrap<R> type, SourceResolver<S, R> sourceResolver) {
        registerSourceResolver(type.getType(), sourceResolver);
    }

    /**
     * Registers the {@link SourceResolver} into an internal registry
     *
     * @param type           the target source valueType
     * @param sourceResolver the source resolver to register
     * @param <R>            the new source valueType parameter
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
