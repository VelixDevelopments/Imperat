package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.help.CommandHelp;
import org.bukkit.command.CommandSender;

public final class BukkitCommandHelp extends CommandHelp<CommandSender> {
	
	private BukkitCommandHelp(BukkitCommandDispatcher dispatcher,
	                          Command<CommandSender> command,
	                          Context<CommandSender> context,
	                          CommandUsage<CommandSender> usage) {
		super(dispatcher, command, context, usage);
	}
	
	public static BukkitCommandHelp create(
					BukkitCommandDispatcher dispatcher,
					Command<CommandSender> command,
					Context<CommandSender> context,
					CommandUsage<CommandSender> usage
	) {
		return new BukkitCommandHelp(dispatcher, command, context, usage);
	}
	
}
