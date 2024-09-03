package dev.velix.imperat;

import com.google.common.reflect.TypeToken;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.suggestions.CompletionArg;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.resolvers.BukkitSuggestionResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

final class BukkitSuggestionResolvers {

    public final static BukkitSuggestionResolver<OfflinePlayer> OFFLINE_PLAYER = new BukkitSuggestionResolver<>() {
        @Override
        public TypeToken<OfflinePlayer> getType() {
            return TypeToken.of(OfflinePlayer.class);
        }

        @Override
        public List<String> autoComplete(
                Command<CommandSender> command,
                CommandSender source,
                ArgumentQueue queue,
                CommandParameter parameterToComplete,
                @Nullable CompletionArg argToComplete
        ) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).toList();
        }
    };

    public final static BukkitSuggestionResolver<Player> PLAYER = new BukkitSuggestionResolver<>() {
        @Override
        public TypeToken<Player> getType() {
            return TypeToken.of(Player.class);
        }

        @Override
        public List<String> autoComplete(
                Command<CommandSender> command,
                CommandSender source,
                ArgumentQueue queue,
                CommandParameter parameterToComplete,
                @Nullable CompletionArg argToComplete
        ) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).toList();
        }
    };

}
