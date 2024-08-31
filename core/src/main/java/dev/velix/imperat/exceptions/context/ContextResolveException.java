package dev.velix.imperat.exceptions.context;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.exceptions.CommandException;

public class ContextResolveException extends CommandException {

    public ContextResolveException(String msg, Object... args) {
        super(String.format(msg, args));
    }

    /**
     * Handles the exception
     *
     * @param imperat the api
     * @param context the context
     */
    @Override
    public <C> void handle(Imperat<C> imperat, Context<C> context) {
        context.getSource().reply("<red>" + msg);
    }

}
