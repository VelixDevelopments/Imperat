package dev.velix.imperat.test;

import dev.velix.imperat.context.Source;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.internal.sur.Pivot;
import dev.velix.imperat.exceptions.CommandException;
import dev.velix.imperat.exceptions.context.ContextResolveException;
import dev.velix.imperat.resolvers.BukkitValueResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class GroupValueResolver implements BukkitValueResolver<Group> {

    @Override
    public Group resolve(
            Source<CommandSender> source,
            Context<CommandSender> context,
            String raw,
            Pivot pivot,
            CommandParameter parameter
    ) throws CommandException {

        var sender = context.getSource();
        if (sender.isConsole()) {
            throw new ContextResolveException("Invalid group '%s'", raw);
        }

        return GroupRegistry.getInstance()
                .getGroup(sender.as(Player.class).getUniqueId());

    }
}
