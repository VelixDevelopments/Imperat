package dev.velix.imperat.test;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.internal.sur.Cursor;
import dev.velix.imperat.exception.SenderErrorException;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.resolvers.BukkitValueResolver;
import org.bukkit.entity.Player;

public final class GroupValueResolver implements BukkitValueResolver<Group> {

    @Override
    public Group resolve(
            BukkitSource source,
            Context<BukkitSource> context,
            String raw,
            Cursor cursor,
            CommandParameter parameter
    ) throws ImperatException {
        var sender = context.getSource();
        if (sender.isConsole()) {
            throw new SenderErrorException("Invalid group '%s'", raw);
        }

        return GroupRegistry.getInstance()
                .getGroup(sender.as(Player.class).getUniqueId());

    }
}
