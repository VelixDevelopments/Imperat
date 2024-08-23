package dev.velix.imperat.test;

import dev.velix.imperat.resolvers.BukkitValueResolver;
import dev.velix.imperat.CommandSource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.exceptions.CommandException;
import dev.velix.imperat.exceptions.context.ContextResolveException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class GroupValueResolver implements BukkitValueResolver<Group> {
	/**
	 * @param source  the source of the command
	 * @param context the context for the command
	 * @param raw     the required parameter of the command.
	 * @return the resolved output from the input object
	 */
	@Override
	public Group resolve(
					CommandSource<CommandSender> source,
					Context<CommandSender> context,
					String raw,
					CommandParameter parameter
	) throws CommandException {
		
		var sender = context.getCommandSource();
		if(sender.isConsole()) {
			throw new ContextResolveException("Invalid group '%s'", raw);
		}
		
		return GroupRegistry.getInstance()
						.getGroup(sender.as(Player.class).getUniqueId());
		
	}
}
