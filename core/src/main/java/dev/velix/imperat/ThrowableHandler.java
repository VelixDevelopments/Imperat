package dev.velix.imperat;

import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ThrowableResolver;
import org.jetbrains.annotations.Nullable;

public sealed interface ThrowableHandler<S extends Source> permits Imperat {

    /**
     * Retrieves the {@link ThrowableResolver} responsible for handling the specified type
     * of throwable. If no specific resolver is found, it may return null or a default resolver.
     *
     * @param exception The class of the throwable to get the resolver for.
     * @param <T>       The type of the throwable.
     * @return The {@link ThrowableResolver} capable of handling the throwable of the specified type,
     * or null if no specific resolver is registered.
     */
    @Nullable
    <T extends Throwable> ThrowableResolver<T, S> getThrowableResolver(final Class<T> exception);

    /**
     * Registers a new {@link ThrowableResolver} for the specified type of throwable.
     * This allows customizing the handling of specific throwable types within the application.
     *
     * @param exception The class of the throwable to set the resolver for.
     * @param handler   The {@link ThrowableResolver} to be registered for the specified throwable type.
     * @param <T>       The type of the throwable.
     */
    <T extends Throwable> void setThrowableResolver(
            final Class<T> exception,
            final ThrowableResolver<T, S> handler
    );

    /**
     * Handles a given throwable by finding the appropriate exception handler or using
     * a default handling strategy if no specific handler is found.
     *
     * @param throwable  The throwable to be handled, which may be an exception or error.
     * @param context    The context in which the throwable occurred, providing necessary information
     *                   about the state and source where the exception happened.
     * @param owning     The class where the throwable originated, used for logging and debugging purposes.
     * @param methodName The name of the method where the throwable was thrown, used for logging and debugging.
     */
    void handleThrowable(
            final Throwable throwable,
            final Context<S> context,
            final Class<?> owning,
            final String methodName
    );

}
