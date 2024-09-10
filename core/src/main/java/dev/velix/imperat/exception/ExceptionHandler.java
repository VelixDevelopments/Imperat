package dev.velix.imperat.exception;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;

public interface ExceptionHandler<E extends Throwable, S extends Source> {

    void handle(final E exception, Imperat<S> imperat, Context<S> context);

}
