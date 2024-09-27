package dev.velix.imperat.command;

import dev.velix.imperat.annotations.base.element.ParameterElement;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.ContextResolver;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a context resolver factory
 * that is responsible for creating {@link ContextResolver}
 *
 * @param <S> the command-sender type
 */
public interface ContextResolverFactory<S extends Source> {


    static <S extends Source, T> @NotNull ContextResolverFactory<S> of(
            Class<T> clazz,
            ContextResolver<S, T> resolver
    ) {
        return (p) -> p != null && TypeUtility.areRelatedTypes(clazz, p.getType()) ? resolver : null;
    }

    /**
     * Creates a context resolver based on the parameter
     *
     * @param parameter the parameter (null if used classic way)
     * @return the {@link ContextResolver} specific for that parameter
     */
    @Nullable
    ContextResolver<S, ?> create(@Nullable ParameterElement parameter);

}
