package studio.mevera.imperat.bukkit.test.commands;

import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.annotations.types.ArgType;
import studio.mevera.imperat.annotations.types.Execute;
import studio.mevera.imperat.annotations.types.Greedy;
import studio.mevera.imperat.annotations.types.Named;
import studio.mevera.imperat.annotations.types.Permission;
import studio.mevera.imperat.annotations.types.RootCommand;
import studio.mevera.imperat.annotations.types.SubCommand;
import studio.mevera.imperat.annotations.types.Suggest;
import studio.mevera.imperat.annotations.types.Switch;

/**
 * Byte-for-byte structural copy of the ItsMyConfig {@code /itsmyconfig}
 * command: one class, several {@code @Execute} subcommands
 * ({@code help}/{@code reload}/{@code message}/{@code parse}/{@code debug}/
 * {@code config}), a permission on root and per method, and root-level
 * alias commands for {@code message} and {@code config}.
 */
@RootCommand("itsmyconfig")
@Permission("itsmyconfig.admin")
public final class RealMessageBrigadierCmd {

    @Execute
    @SubCommand("help")
    public void help(BukkitCommandSource source) {
    }

    @SubCommand("reload")
    @Permission("itsmyconfig.reload")
    public void reload(BukkitCommandSource source) {
    }

    @SubCommand("message")
    @Permission("itsmyconfig.message")
    public void message(
            BukkitCommandSource source,
            @Named("target") @ArgType(TestSelectorArgumentType.class) String target,
            @Switch("direct") boolean direct,
            @Named("message") @Greedy @Suggest({"hello", "world"}) String message
    ) {
    }

    @SubCommand("parse")
    @Permission("itsmyconfig.parse")
    public void parse(
            BukkitCommandSource source,
            @Named("target") @ArgType(TestSelectorArgumentType.class) String target,
            @Named("message") @Greedy @Suggest({"hello", "world"}) String message
    ) {
    }

    @SubCommand("debug")
    @Permission("itsmyconfig.debug")
    public void debug(BukkitCommandSource source) {
    }

    @SubCommand("config")
    @Permission("itsmyconfig.config")
    public void config(
            BukkitCommandSource source,
            @Named("placeholder") String placeholder,
            @Named("value") @Greedy @Suggest({"hello", "world"}) String value
    ) {
    }

    @RootCommand("message")
    @Permission("itsmyconfig.message")
    public void msgCommand(
            BukkitCommandSource source,
            @Named("target") @ArgType(TestSelectorArgumentType.class) String target,
            @Switch("direct") boolean direct,
            @Named("message") @Greedy @Suggest({"hello", "world"}) String message
    ) {
    }

    @RootCommand("config")
    @Permission("itsmyconfig.config")
    public void configCommand(
            BukkitCommandSource source,
            @Named("placeholder") String placeholder,
            @Named("value") @Greedy @Suggest({"hello", "world"}) String value
    ) {
    }
}