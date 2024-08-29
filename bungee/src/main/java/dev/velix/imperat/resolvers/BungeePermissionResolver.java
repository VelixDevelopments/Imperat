package dev.velix.imperat.resolvers;

import dev.velix.imperat.Source;
import net.md_5.bungee.api.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BungeePermissionResolver implements PermissionResolver<CommandSender> {

    @Override
    public boolean hasPermission(
            @NotNull Source<CommandSender> source,
            @Nullable String permission
    ) {
        return source.getOrigin().hasPermission(permission);
    }
}
