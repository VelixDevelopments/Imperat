package studio.mevera.imperat.bukkit.test.commands;

import org.jetbrains.annotations.NotNull;
import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.command.arguments.Argument;
import studio.mevera.imperat.command.arguments.type.SimpleArgumentType;
import studio.mevera.imperat.context.CommandContext;
import studio.mevera.imperat.exception.CommandException;

import java.util.List;

/**
 * Fixture mimicking a strict selector argument (like a player selector): a
 * single-token type that only accepts known player names and throws on any
 * other input, with player-name suggestions.
 */
public final class TestSelectorArgumentType extends SimpleArgumentType<BukkitCommandSource, String> {

    private static final List<String> PLAYERS = List.of("TestPlayer", "Notch", "Herobrine");

    public TestSelectorArgumentType() {
        super(1);
        addStaticSuggestions("TestPlayer", "Notch", "Herobrine");
    }

    @Override
    public String parse(
            @NotNull CommandContext<BukkitCommandSource> context,
            @NotNull Argument<BukkitCommandSource> argument,
            @NotNull String input
    ) throws CommandException {
        if (!PLAYERS.contains(input)) {
            throw new CommandException("Unknown player '" + input + "'");
        }
        return input;
    }
}
