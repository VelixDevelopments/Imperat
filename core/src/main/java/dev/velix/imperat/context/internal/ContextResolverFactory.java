package dev.velix.imperat.context.internal;

import dev.velix.imperat.resolvers.ContextResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;

/**
 * Represents a context resolver factory
 * that is responsible for creating {@link ContextResolver}
 *
 * @param <C> the command-sender type
 */
public interface ContextResolverFactory<C> {


    /**
     * Creates a context resolver based on the parameter
     *
     * @param parameter the parameter
     * @return the {@link ContextResolver} specific for that parameter
     */
    @Nullable
    ContextResolver<C, ?> create(@NotNull Parameter parameter);


    static <C, T> @NotNull ContextResolverFactory<C> of(
            Class<T> clazz,
            ContextResolver<C, T> resolver
    ) {
        return (p) -> p.getType().isAssignableFrom(clazz) ? resolver : null;
    }

}
