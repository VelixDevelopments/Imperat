package dev.velix.imperat.test;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.sur.Cursor;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.resolvers.BukkitValueResolver;
import org.bukkit.entity.Player;

public final class GroupValueResolver implements BukkitValueResolver<Group> {
    
    @Override
    public Group resolve(
            ExecutionContext<BukkitSource> context,
            CommandParameter parameter,
            Cursor cursor,
            String raw
    ) throws ImperatException {
        
        var sender = context.getSource();
        if (sender.isConsole()) {
            throw new SourceException("Only players can do this !");
        }
        
        var playerGroup = GroupRegistry.getInstance()
                .getGroup(sender.as(Player.class).getUniqueId());
        
        if (playerGroup == null) {
            throw new SourceException("Invalid group '%s'", raw);
        }
        
        return playerGroup;
    }
}
