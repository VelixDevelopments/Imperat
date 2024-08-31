package dev.velix.imperat.exceptions;

import dev.velix.imperat.Imperat;
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

    protected CommandException() {
        this("");
    }
    /**
     * Handles the exception
     *
     * @param <C>     the command-sender type
     * @param imperat the api
     * @param context the context
     */
    public abstract <C> void handle(Imperat<C> imperat, Context<C> context);

}
