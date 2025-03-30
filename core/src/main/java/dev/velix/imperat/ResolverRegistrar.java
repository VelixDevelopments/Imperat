package dev.velix.imperat;

import dev.velix.imperat.command.ContextResolverFactory;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.placeholders.Placeholder;
import dev.velix.imperat.resolvers.ContextResolver;
import dev.velix.imperat.resolvers.SourceResolver;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.*;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public sealed interface ResolverRegistrar<S extends Source> permits ImperatConfig {


    /**
     * Registers a context resolver factory
     *
     * @param factory the factory to register
     */
    <T> void registerContextResolverFactory(Type type, ContextResolverFactory<S, T> factory);


    /**
     * Registers {@link ContextResolver}
     *
     * @param type     the class-valueType of value being resolved from context
     * @param resolver the resolver for this value
     * @param <T>      the valueType of value being resolved from context
     */
    <T> void registerContextResolver(Type type, @NotNull ContextResolver<S, T> resolver);


    /**
     * Registers {@link ParameterType}
     *
     * @param type     the class-valueType of value being resolved from context
     * @param resolver the resolver for this value
     * @param <T>      the valueType of value being resolved from context
     */
    <T> void registerParamType(Class<T> type, @NotNull ParameterType<S, T> resolver);

    /**
     * Registers a supplier function that provides new instances of a specific Collection type.
     * This allows the framework to create appropriate collection instances during deserialization
     * or initialization processes.
     *
     * @param <C> the type of Collection to be initialized
     * @param collectionType the Class object representing the collection type
     * @param newInstanceSupplier a Supplier that creates new instances of the collection type
     * @throws NullPointerException if collectionType or newInstanceSupplier is null
     */
    <C extends Collection<?>> void registerCollectionInitializer(Class<C> collectionType, Supplier<C> newInstanceSupplier);

    /**
     * Registers a supplier function that provides new instances of a specific Map type.
     * This allows the framework to create appropriate map instances during deserialization
     * or initialization processes.
     *
     * @param <M> the type of Map to be initialized
     * @param mapType the Class object representing the map type
     * @param newInstanceSupplier a Supplier that creates new instances of the map type
     * @throws NullPointerException if mapType or newInstanceSupplier is null
     */
    <M extends Map<?, ?>> void registerMapInitializer(Class<M> mapType, Supplier<M> newInstanceSupplier);

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
            ImperatDebugger.debug("Found no specific argument suggestion resolver for param '%s'", parameter.format());
            var resolverByType = parameter.type().getSuggestionResolver();

            if(resolverByType != null)
                ImperatDebugger.debug("Found resolver by type for param '%s'", parameter.format() );
            else
                ImperatDebugger.debug("Resolving suggestion in the form of the parameter's literal format !");

            return Objects.requireNonNullElseGet(resolverByType, () -> SuggestionResolver.plain(Collections.singletonList(parameter.format())));
        } else {
            ImperatDebugger.debug("Found specific parameter suggestion for param '%s'", parameter.format());
            return parameterSpecificResolver;
        }
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

    /**
     * Registers a placeholder
     *
     * @param placeholder to register
     */
    void registerPlaceholder(Placeholder<S> placeholder);


}
