package dev.velix.imperat.exceptions.context;

import dev.velix.imperat.context.Context;
import dev.velix.imperat.exceptions.CommandException;

public class ContextResolveException extends CommandException {

    public ContextResolveException(String msg, Object... args) {
        super(String.format(msg, args));
    }

    /**
     * Handles the exception
     *
     * @param context the context
     */
    @Override
    public <C> void handle(Context<C> context) {
        context.getSource().reply("<red>" + msg);
    }

}
