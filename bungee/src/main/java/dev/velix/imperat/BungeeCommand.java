package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import net.md_5.bungee.api.CommandSender;

public interface BungeeCommand {
	
	static Command.Builder<CommandSender> create(String name) {
		return Command.create(name);
	}
	
}
