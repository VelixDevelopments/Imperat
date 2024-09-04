package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import org.bukkit.command.CommandSender;

public interface BukkitCommand {
	
	static Command.Builder<CommandSender> create(String name) {
		return Command.create(name);
	}
	
}
