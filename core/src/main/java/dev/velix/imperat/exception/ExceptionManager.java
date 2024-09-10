package dev.velix.imperat.exception;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.CommandDebugger;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class ExceptionManager<S extends Source> {

    private final Map<Class<? extends Throwable>, ExceptionHandler<? extends Throwable, S>> handlers = new HashMap<>();

    public ExceptionManager() {
        this.register(SenderErrorException.class, (exception, imperat, context) -> context.getSource().error(exception.getMessage()));
    }

    public <E extends Throwable> void register(final Class<E> exception, final ExceptionHandler<E, S> handler) {
        this.handlers.put(exception, handler);
    }

    @Nullable
    public <E extends Throwable> ExceptionHandler<E, S> get(final Class<E> exception) {
        Class<?> current = exception;
        while (current != null && Throwable.class.isAssignableFrom(current)) {
            if (handlers.containsKey(current)) {
                return (ExceptionHandler<E, S>) handlers.get(current);
            }
            current = current.getSuperclass();
        }
        return null;
    }

    public void handle(
            final Throwable error,
            final Imperat<S> imperat,
            final Context<S> context,
            final Class<?> owningClass,
            final String methodName
    ) {
        Throwable current = error;

        while (current != null) {
            if (current instanceof SelfHandledException selfHandledException) {
                selfHandledException.handle(imperat, context);
                return;
            }

            ExceptionHandler<? super Throwable, S> handler = (ExceptionHandler<? super Throwable, S>) this.get(current.getClass());
            if (handler != null) {
                handler.handle(current, imperat, context);
                return;
            }

            current = current.getCause();
        }

        CommandDebugger.error(owningClass, methodName, error);
    }

}
