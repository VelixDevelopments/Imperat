package dev.velix.imperat.test.guild;

import dev.velix.imperat.context.Context;
import dev.velix.imperat.exceptions.CommandException;
import dev.velix.imperat.exceptions.context.ContextResolveException;
import dev.velix.imperat.resolvers.BukkitContextResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;

public final class GuildContextResolver implements BukkitContextResolver<Guild> {

    /**
     * Resolves a parameter's default value
     * if it has been not input by the user
     *
     * @param context   the context
     * @param parameter the parameter (null if used the classic way)
     * @return the resolved default-value
     */
    @Override
    public @Nullable Guild resolve(
            @NotNull Context<CommandSender> context,
            @Nullable Parameter parameter
    ) throws CommandException {
        var source = context.getSource();
        if (source.isConsole()) {
            throw new ContextResolveException("Only a player can do this !");
        }
        Player player = source.as(Player.class);
        return GuildRegistry.getInstance().getUserGuild(player.getUniqueId());
    }
}
