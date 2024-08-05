package dev.velix.imperat.exceptions.context;

import dev.velix.imperat.context.Context;
import dev.velix.imperat.exceptions.CommandException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ContextResolveException extends CommandException {

	public ContextResolveException(String msg) {
		super(msg);
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
