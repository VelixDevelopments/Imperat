package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.help.CommandHelp;
import net.md_5.bungee.api.CommandSender;

public final class BungeeCommandHelp extends CommandHelp<CommandSender> {
	private BungeeCommandHelp(
					CommandDispatcher<CommandSender> dispatcher,
					Command<CommandSender> command,
					Context<CommandSender> context,
					CommandUsage<CommandSender> usage
	) {
		super(dispatcher, command, context, usage);
	}
	
	public static BungeeCommandHelp create(
					BungeeCommandDispatcher dispatcher,
					Command<CommandSender> command,
					Context<CommandSender> context,
					CommandUsage<CommandSender> usage
	) {
		return new BungeeCommandHelp(dispatcher, command, context, usage);
	}
	
}
