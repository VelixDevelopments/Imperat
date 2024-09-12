package dev.velix.imperat.test.guild;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.annotations.element.ParameterElement;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SenderErrorException;
import dev.velix.imperat.resolvers.BukkitContextResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            @NotNull Context<BukkitSource> context,
            @Nullable ParameterElement parameter
    ) throws ImperatException {
        var source = context.getSource();
        if (source.isConsole()) {
            throw new SenderErrorException("Only a player can do this !");
        }
        Player player = source.as(Player.class);
        return GuildRegistry.getInstance().getUserGuild(player.getUniqueId());
    }
}
