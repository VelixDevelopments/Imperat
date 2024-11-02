package dev.velix.imperat.exception;

import dev.velix.imperat.ImperatConfig;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;

public interface ThrowableResolver<E extends Throwable, S extends Source> {

    void resolve(final E exception, ImperatConfig<S> imperat, Context<S> context);

}
