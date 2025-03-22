package dev.velix.imperat.resolvers;

import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.*;

/**
 * Represents a functional way of checking for the permissions
 * of the command source/sender.
 */
@ApiStatus.AvailableSince("1.0.0")
public interface PermissionResolver<S extends Source> {

    /**
     * @param source     the source of the command (console or other)
     * @param permission the permission
     * @return whether this command source/sender has a specific permission
     */
    boolean hasPermission(@NotNull S source, @Nullable String permission);

    default boolean hasUsagePermission(S source, @Nullable CommandUsage<S> usage) {
        if (usage == null || usage.permission() == null) {
            return true;
        }
        if (!hasPermission(source, usage.permission())) return false;
        for (CommandParameter<S> parameter : usage.getParameters()) {
            //if (!parameter.isCommand()) continue;
            if (parameter.permission() == null) continue;
            if (!hasPermission(source, parameter.permission()))
                return false;
        }
        return true;
    }

}
