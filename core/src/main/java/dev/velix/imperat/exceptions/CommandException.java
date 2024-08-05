package dev.velix.imperat.exceptions;

import dev.velix.imperat.context.Context;

/**
 * Defines an exception that occurs during context
 */
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
