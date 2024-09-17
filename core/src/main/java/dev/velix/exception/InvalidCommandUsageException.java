package dev.velix.exception;

import dev.velix.command.Command;
import dev.velix.command.CommandUsage;
import dev.velix.context.Source;

public final class InvalidCommandUsageException extends RuntimeException {
    
    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public <S extends Source> InvalidCommandUsageException(
            final Command<S> command,
            final CommandUsage<S> usage
    ) {
        super(
                String.format("Invalid command usage: '%s'", CommandUsage.format(command, usage))
        );
    }
    
}
