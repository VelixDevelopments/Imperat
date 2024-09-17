package dev.velix.test.guild;

import dev.velix.BukkitSource;
import dev.velix.annotations.base.element.ParameterElement;
import dev.velix.context.ExecutionContext;
import dev.velix.exception.ImperatException;
import dev.velix.exception.SourceException;
import dev.velix.resolvers.BukkitContextResolver;
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
    public @NotNull Guild resolve(
            @NotNull ExecutionContext<BukkitSource> context,
            @Nullable ParameterElement parameter
    ) throws ImperatException {
        var source = context.getSource();
        if (source.isConsole()) {
            throw new SourceException("Only a player can do this !");
        }
        Player player = source.as(Player.class);
        Guild guild = GuildRegistry.getInstance().getUserGuild(player.getUniqueId());
        if (guild == null) {
            throw new SourceException("You don't have a guild !");
        }
        return guild;
    }
}
