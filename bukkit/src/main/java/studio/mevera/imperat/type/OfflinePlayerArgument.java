package studio.mevera.imperat.type;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.annotations.types.OnlineOnly;
import studio.mevera.imperat.command.arguments.Argument;
import studio.mevera.imperat.command.arguments.type.SimpleArgumentType;
import studio.mevera.imperat.context.CommandContext;
import studio.mevera.imperat.context.SuggestionContext;
import studio.mevera.imperat.exception.ArgumentParseException;
import studio.mevera.imperat.exception.CommandException;
import studio.mevera.imperat.providers.SuggestionProvider;
import studio.mevera.imperat.responses.BukkitResponseKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Server-side resolver for {@link OfflinePlayer} parameters. Generic
 * over the canonical source type {@code S} so custom-source plugins can
 * register cleanly without raw casts.
 */
public class OfflinePlayerArgument<S extends BukkitCommandSource> extends SimpleArgumentType<S, OfflinePlayer> {


    private final PlayerSuggestionProvider<S> playerSuggestionResolver = new PlayerSuggestionProvider<>();

    public OfflinePlayerArgument() {
        super();
    }

    @Override
    public OfflinePlayer parse(@NotNull CommandContext<S> context, @NonNull Argument<S> argument,
            @NotNull String input) throws CommandException {
        if (input.length() > 16) {
            throw new ArgumentParseException(BukkitResponseKey.UNKNOWN_OFFLINE_PLAYER, input);
        }
        return Bukkit.getOfflinePlayer(input);
    }

    @Override
    public SuggestionProvider<S> getSuggestionProvider() {
        return playerSuggestionResolver;
    }

    private final static class PlayerSuggestionProvider<S extends BukkitCommandSource> implements SuggestionProvider<S> {

        private static List<String> cachedNames;
        private static long lastRefresh;

        private static List<String> names() {
            long now = System.currentTimeMillis();
            if (cachedNames == null || now - lastRefresh > 300_000) {
                var names = new ArrayList<String>();
                for (var player : Bukkit.getOfflinePlayers()) {
                    var name = player.getName();
                    if (name != null) names.add(name);
                }
                cachedNames = names;
                lastRefresh = now;
            }
            return cachedNames;
        }

        /**
         * @param context   the context for suggestions
         * @param argument the parameter of the value to complete
         * @return the auto-completed suggestions of the current argument
         */
        @Override
        public List<String> provide(SuggestionContext<S> context, Argument<S> argument) {
            final String prefix = context.getArgToComplete().value();
            final String lowerPrefix = prefix.toLowerCase();
            final boolean onlineOnly = argument.isAnnotated()
                    && argument.asAnnotatedArgument().hasAnnotation(OnlineOnly.class);

            var names = new HashSet<String>();

            if (!onlineOnly) {
                for (var name : names()) {
                    if (name.toLowerCase().startsWith(lowerPrefix)) {
                        names.add(name);
                    }
                }
            }

            for (var player : Bukkit.getOnlinePlayers()) {
                var name = player.getName();
                if (name.toLowerCase().startsWith(lowerPrefix)) {
                    names.add(name);
                }
            }

            return new ArrayList<>(names);
        }
    }
}
