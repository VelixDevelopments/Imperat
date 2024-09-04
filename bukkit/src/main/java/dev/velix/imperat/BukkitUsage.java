package dev.velix.imperat;

import dev.velix.imperat.command.CommandUsage;
import org.bukkit.command.CommandSender;

public interface BukkitUsage {
	
	static CommandUsage.Builder<CommandSender> builder() {
		return CommandUsage.builder();
	}
	
}
