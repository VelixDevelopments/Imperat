package dev.velix.imperat.type;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.Imperat;
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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ParameterPlayer extends BaseParameterType<BukkitSource, Player> {

    private final PlayerSuggestionResolver SUGGESTION_RESOLVER = new PlayerSuggestionResolver();

    public ParameterPlayer() {
        super(TypeWrap.of(Player.class));
    }

    @Override
    public @Nullable Player resolve(
            @NotNull ExecutionContext<BukkitSource> context,
            @NotNull CommandInputStream<BukkitSource> commandInputStream,
            String input) throws ImperatException {
        String raw = commandInputStream.currentRaw().orElse(null);
        if (raw == null) return null;

        if (raw.equalsIgnoreCase("me")) {
            if (context.source().isConsole()) {
                throw new UnknownPlayerException(raw);
            }
            return context.source().asPlayer();
        }
        final Player player = Bukkit.getPlayer(raw.toLowerCase());
        if (player != null) return player;
        throw new UnknownPlayerException(raw);
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<BukkitSource> parameter) {
        return input.length() <= 16;
    }

    @Override
    public @NotNull Player fromString(Imperat<BukkitSource> imperat, String input) throws ImperatException {
        return Objects.requireNonNull(Bukkit.getPlayer(input));
    }

    /**
     * Returns the suggestion resolver associated with this parameter type.
     *
     * @return the suggestion resolver for generating suggestions based on the parameter type.
     */
    @Override
    public SuggestionResolver<BukkitSource> getSuggestionResolver() {
        return SUGGESTION_RESOLVER;
    }

    private final static class PlayerSuggestionResolver implements SuggestionResolver<BukkitSource> {

        /**
         * @param context   the context for suggestions
         * @param parameter the parameter of the value to complete
         * @return the auto-completed suggestions of the current argument
         */
        @Override
        public List<String> autoComplete(SuggestionContext<BukkitSource> context, CommandParameter<BukkitSource> parameter) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
    }
}
