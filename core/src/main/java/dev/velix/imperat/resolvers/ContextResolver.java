package dev.velix.imperat.resolvers;

import dev.velix.imperat.context.Context;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;
import java.util.function.Supplier;

/**
 * Resolves a default non-overridable value for
 * missing required arguments
 *
 * @param <T> the type of resolver's parameter
 */
public interface ContextResolver<C, T> {

    /**
     * Resolves a parameter's default value
     * if it has been not input by the user
     *
     * @param context   the context
     * @param parameter the parameter
     * @return the resolved default-value
     */
    @Nullable
    T resolve(
            Context<C> context,
            Parameter parameter
    );

    static <C, T> ContextResolver<C, T> of(T value) {
        return (c, p) -> value;
    }

    static <C, T> ContextResolver<C, T> of(Supplier<T> supplier) {
        return of(supplier.get());
    }

}
