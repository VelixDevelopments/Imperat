package dev.velix.imperat;

import dev.velix.imperat.command.CommandUsage;
import net.md_5.bungee.api.CommandSender;

public interface BungeeUsage {
	
	static CommandUsage.Builder<CommandSender> builder() {
		return CommandUsage.builder();
	}
	
}
