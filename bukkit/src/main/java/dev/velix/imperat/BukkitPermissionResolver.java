package dev.velix.imperat;

import dev.velix.imperat.resolvers.PermissionResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BukkitPermissionResolver implements PermissionResolver<BukkitSource> {
    
    
    /**
     * @param source     the source of the command (console or other)
     * @param permission the permission
     * @return whether this command source/sender has a specific permission
     */
    @Override
    public boolean hasPermission(
            @NotNull BukkitSource source,
            @Nullable String permission
    ) {
        if (permission == null || permission.isEmpty()) return true;
        return source.origin().hasPermission(permission);
    }
    
}
