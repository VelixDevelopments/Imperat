package dev.velix.exception;

import dev.velix.Imperat;
import dev.velix.context.Context;
import dev.velix.context.Source;

public interface ThrowableResolver<E extends Throwable, S extends Source> {
    
    void resolve(final E exception, Imperat<S> imperat, Context<S> context);
    
}
