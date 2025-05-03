package dev.velix.imperat.type;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.UnknownPlayerException;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.TypeWrap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class ParameterOfflinePlayer extends BaseParameterType<BukkitSource, OfflinePlayer> {

    protected final boolean ifCachedOnly;

    private final PlayerSuggestionResolver playerSuggestionResolver = new PlayerSuggestionResolver();
    public ParameterOfflinePlayer(boolean ifCachedOnly) {
        super(TypeWrap.of(OfflinePlayer.class));
        this.ifCachedOnly = ifCachedOnly;
    }

    public ParameterOfflinePlayer() {
        this(false);
    }

    @Override
    public @Nullable OfflinePlayer resolve(
            @NotNull ExecutionContext<BukkitSource> context,
            @NotNull CommandInputStream<BukkitSource> commandInputStream,
            String input) throws ImperatException {

        if (input.length() > 16) {
            throw new UnknownPlayerException(input);
        }

        return Bukkit.getOfflinePlayer(input);
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<BukkitSource> parameter) {
        return input.length() <= 16;
    }


    @Override
    public SuggestionResolver<BukkitSource> getSuggestionResolver() {
        return playerSuggestionResolver;
    }

    private final static class PlayerSuggestionResolver implements SuggestionResolver<BukkitSource> {

        /**
         * @param context   the context for suggestions
         * @param parameter the parameter of the value to complete
         * @return the auto-completed suggestions of the current argument
         */
        @Override
        public List<String> autoComplete(SuggestionContext<BukkitSource> context, CommandParameter<BukkitSource> parameter) {
            return Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .toList();
        }
    }
}
