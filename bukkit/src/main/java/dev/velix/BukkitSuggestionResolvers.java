package dev.velix;


import dev.velix.command.parameters.CommandParameter;
import dev.velix.context.SuggestionContext;
import dev.velix.resolvers.BukkitSuggestionResolver;
import dev.velix.util.TypeWrap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

final class BukkitSuggestionResolvers {
    
    public final static BukkitSuggestionResolver<OfflinePlayer> OFFLINE_PLAYER = new BukkitSuggestionResolver<>() {
        @Override
        public TypeWrap<OfflinePlayer> getType() {
            return TypeWrap.of(OfflinePlayer.class);
        }
        
        @Override
        public List<String> autoComplete(SuggestionContext<BukkitSource> context, CommandParameter parameterToComplete) {
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
        public List<String> autoComplete(SuggestionContext<BukkitSource> context, CommandParameter parameterToComplete) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).toList();
        }
        
    };
    
}
