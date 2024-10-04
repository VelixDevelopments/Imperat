package dev.velix.imperat.exception;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;

public interface ThrowableResolver<E extends Throwable, S extends Source> {
	
	void resolve(final E exception, Imperat<S> imperat, Context<S> context);
	
}
