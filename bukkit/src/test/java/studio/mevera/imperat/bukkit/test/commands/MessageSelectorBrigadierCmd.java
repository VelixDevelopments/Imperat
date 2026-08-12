package studio.mevera.imperat.bukkit.test.commands;

import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.annotations.types.ArgType;
import studio.mevera.imperat.annotations.types.Greedy;
import studio.mevera.imperat.annotations.types.Named;
import studio.mevera.imperat.annotations.types.RootCommand;
import studio.mevera.imperat.annotations.types.SubCommand;
import studio.mevera.imperat.annotations.types.Suggest;
import studio.mevera.imperat.annotations.types.Switch;

/**
 * Faithful mirror of the ItsMyConfig {@code /itsmyconfig message} command:
 * a literal subcommand followed by a strict-selector {@code target}, a
 * switch, and a trailing greedy {@code message}.
 */
@RootCommand("msgsel")
public final class MessageSelectorBrigadierCmd {

    @SubCommand("message")
    public void message(
            BukkitCommandSource source,
            @Named("target") @ArgType(TestSelectorArgumentType.class) String target,
            @Switch("direct") boolean direct,
            @Named("message") @Greedy @Suggest({"hello", "world"}) String message
    ) {
    }
}