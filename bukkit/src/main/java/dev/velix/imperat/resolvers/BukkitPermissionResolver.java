package dev.velix.imperat.resolvers;

import dev.velix.imperat.Source;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BukkitPermissionResolver implements PermissionResolver<CommandSender> {


    /**
     * @param source     the source of the command (console or other)
     * @param permission the permission
     * @return whether this command source/sender has a specific permission
     */
    @Override
    public boolean hasPermission(
            @NotNull Source<CommandSender> source,
            @Nullable String permission
    ) {
        if (permission == null || permission.isEmpty()) return true;
        return source.getOrigin().hasPermission(permission);
    }

}
