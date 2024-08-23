package dev.velix.imperat.exceptions.context;

import dev.velix.imperat.context.Context;
import dev.velix.imperat.exceptions.CommandException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

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
        context.getCommandSource()
                .reply(Component.text(msg, NamedTextColor.RED));
    }

}
