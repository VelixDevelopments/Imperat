package dev.velix.imperat.resolvers;

import dev.velix.imperat.BungeeSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BungeePermissionResolver implements PermissionResolver<BungeeSource> {

    @Override
    public boolean hasPermission(
        @NotNull BungeeSource source,
        @Nullable String permission
    ) {
        return source.origin().hasPermission(permission);
    }
}
