package dev.velix.imperat.context.internal;

import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.ContextResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;

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
        return (p) -> p != null && p.getType().isAssignableFrom(clazz) ? resolver : null;
    }
    
    /**
     * Creates a context resolver based on the parameter
     *
     * @param parameter the parameter (null if used classic way)
     * @return the {@link ContextResolver} specific for that parameter
     */
    @Nullable
    ContextResolver<S, ?> create(@Nullable Parameter parameter);
    
}
