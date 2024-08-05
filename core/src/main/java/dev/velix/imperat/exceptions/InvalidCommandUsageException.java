package dev.velix.imperat.exceptions;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;

public final class InvalidCommandUsageException extends RuntimeException {

	/**
	 * Constructs a new runtime exception with the specified detail message.
	 * The cause is not initialized, and may subsequently be initialized by a
	 * call to {@link #initCause}.
	 */
	public <C> InvalidCommandUsageException(Command<C> command,
	                                        CommandUsage<C> usage) {
		super(
				  String.format("Invalid command usage: '%s'", CommandUsage.format(command, usage))
		);
	}
}
