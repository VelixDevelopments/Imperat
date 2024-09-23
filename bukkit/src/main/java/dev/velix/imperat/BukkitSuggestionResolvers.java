package dev.velix.imperat;


import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.resolvers.BukkitSuggestionResolver;
import dev.velix.imperat.util.TypeWrap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;

final class BukkitSuggestionResolvers {
    
    public final static BukkitSuggestionResolver<OfflinePlayer> OFFLINE_PLAYER = new BukkitSuggestionResolver<>() {
        @Override
        public TypeWrap<OfflinePlayer> getType() {
            return TypeWrap.of(OfflinePlayer.class);
        }
        
        @Override
        public Collection<String> autoComplete(SuggestionContext<BukkitSource> context, CommandParameter<BukkitSource> parameterToComplete) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).toList();
        }
    };
    
    public final static BukkitSuggestionResolver<Player> PLAYER = new BukkitSuggestionResolver<>() {
        @Override
        public TypeWrap<Player> getType() {
            return TypeWrap.of(Player.class);
        }
        
        @Override
        public Collection<String> autoComplete(SuggestionContext<BukkitSource> context, CommandParameter<BukkitSource> parameterToComplete) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).toList();
        }
    };
    
}
