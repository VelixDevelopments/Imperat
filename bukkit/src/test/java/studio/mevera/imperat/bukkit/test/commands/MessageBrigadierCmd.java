package studio.mevera.imperat.bukkit.test.commands;

import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.annotations.types.Greedy;
import studio.mevera.imperat.annotations.types.Named;
import studio.mevera.imperat.annotations.types.RootCommand;
import studio.mevera.imperat.annotations.types.SubCommand;
import studio.mevera.imperat.annotations.types.Suggest;
import studio.mevera.imperat.annotations.types.Switch;

/**
 * Regression shape for the ItsMyConfig {@code /itsmyconfig message} command:
 * a literal subcommand followed by a positional {@code target}, a switch, and
 * a trailing greedy {@code message}. Exercises the positional-before-greedy
 * suggestion flow in Brigadier.
 */
@RootCommand("msgtest")
public final class MessageBrigadierCmd {

    @SubCommand("message")
    public void message(
            BukkitCommandSource source,
            @Named("target") @Suggest({"TestPlayer", "Notch", "Herobrine"}) String target,
            @Switch("direct") boolean direct,
            @Named("message") @Greedy @Suggest({"hello", "world"}) String message
    ) {
    }
}
