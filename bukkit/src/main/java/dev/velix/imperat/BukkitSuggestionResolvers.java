package dev.velix.imperat;

import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.resolvers.TypeSuggestionResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;

final class BukkitSuggestionResolvers {
    
    public final static TypeSuggestionResolver<BukkitSource, OfflinePlayer> OFFLINE_PLAYER =
            SuggestionResolver.type(OfflinePlayer.class, Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList());
    
    public final static TypeSuggestionResolver<BukkitSource, Player> PLAYER =
            SuggestionResolver.type(Player.class, Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).toList());
    
}
