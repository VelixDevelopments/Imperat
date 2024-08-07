package dev.velix.imperat.exceptions;

import dev.velix.imperat.context.Context;
import org.jetbrains.annotations.ApiStatus;

/**
 * Defines an exception that occurs during context
 */
@ApiStatus.AvailableSince("1.0.0")
public abstract class CommandException extends Exception {

	protected final String msg;

	protected CommandException(String msg) {
		this.msg = msg;
	}

	/**
	 * Handles the exception
	 *
	 * @param context the context
	 * @param <C>     the command-sender type
	 */
	public abstract <C> void handle(Context<C> context);

}
