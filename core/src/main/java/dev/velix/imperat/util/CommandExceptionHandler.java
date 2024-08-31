package dev.velix.imperat.util;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.exceptions.CommandException;

public final class CommandExceptionHandler {
	
	private CommandExceptionHandler() {
		throw new AssertionError();
	}
	
	public static <C> void handleException(
					final Imperat<C> imperat,
					final Context<C> context,
					final Class<?> owningClass,
					final String methodName,
					final Throwable error
	) {
	    Throwable current = error;
	    if (current instanceof CommandException commandException) {
	        commandException.handle(imperat, context);
	        return;
	    }
	
	    do {
	        current = current.getCause();
	    } while (current != null && !(current instanceof CommandException));
	
	    if (current != null) {
	        ((CommandException) current).handle(imperat, context);
	    } else {
	        CommandDebugger.error(owningClass, methodName, error);
	    }
	
	}
	
	
}
