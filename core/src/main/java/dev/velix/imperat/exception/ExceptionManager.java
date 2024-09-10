package dev.velix.imperat.exception;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.CommandDebugger;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class ExceptionManager<S extends Source> {

    private final Map<Class<? extends Throwable>, ExceptionHandler<? extends Throwable, S>> handlers = new HashMap<>();

    public <E extends Throwable> void register(final Class<E> exception, final ExceptionHandler<E, S> handler) {
        this.handlers.put(exception, handler);
    }

    public <E extends Throwable> ExceptionHandler<E, S> get(final Class<E> exception) {
        return (ExceptionHandler<E, S>) this.handlers.get(exception);
    }

    public void handle(
            final Imperat<S> imperat,
            final Context<S> context,
            final Class<?> owningClass,
            final String methodName,
            final Throwable error
    ) {
        Throwable current = error;
        do {
            if (current instanceof SelfHandledException selfHandledException) {
                selfHandledException.handle(imperat, context);
                return;
            }
            current = current.getCause();
        } while (current != null && this.get(current.getClass()) == null);

        if (current == null) {
            CommandDebugger.error(owningClass, methodName, error);
            return;
        }

        if (current instanceof SelfHandledException selfHandledException) {
            selfHandledException.handle(imperat, context);
            return;
        }

        final ExceptionHandler<Throwable, S> handler = (ExceptionHandler<Throwable, S>) this.get(current.getClass());
        if (handler != null) {
            handler.handle(current, imperat, context);
            return;
        }

        CommandDebugger.error(owningClass, methodName, error);
    }

}
