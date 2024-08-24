package dev.velix.imperat.examples.exceptions;

import dev.velix.imperat.context.Context;
import dev.velix.imperat.exceptions.CommandException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ExampleCustomException extends CommandException {
	
	
	public ExampleCustomException(String msg) {
		super(msg);
	}
	
	@Override
	public <C> void handle(Context<C> context) {
		var source = context.getCommandSource();
		source.reply(Component.text(msg, NamedTextColor.RED));
	}
	
}
