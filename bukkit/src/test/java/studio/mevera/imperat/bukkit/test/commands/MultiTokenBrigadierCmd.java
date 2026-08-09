package studio.mevera.imperat.bukkit.test.commands;

import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.annotations.types.ArgType;
import studio.mevera.imperat.annotations.types.Default;
import studio.mevera.imperat.annotations.types.Execute;
import studio.mevera.imperat.annotations.types.Flag;
import studio.mevera.imperat.annotations.types.Named;
import studio.mevera.imperat.annotations.types.RootCommand;
import studio.mevera.imperat.annotations.types.SubCommand;
import studio.mevera.imperat.annotations.types.Suggest;

/**
 * Fixture for the multi-token Brigadier chain: a fixed 4-token positional
 * ({@code <x> <y> <z> <w>}) followed by a required text argument, an
 * optional world, a value flag, and a subcommand. The chain must render as
 * 4 Brigadier segments and the subcommand / optional / flag must only
 * surface after all four tokens have been typed.
 */
@RootCommand("mt")
public final class MultiTokenBrigadierCmd {

    @Execute
    public void root(
            BukkitCommandSource src,
            @Named("coord") @ArgType(QuadCoordArgumentType.class) QuadCoord coord,
            @Named("label") String label,
            @Default("overworld") @Named("world") String world,
            @Flag({"mode", "m"}) @Suggest({"safe", "wild"}) String mode
    ) {
    }

    @SubCommand("info")
    public void info(BukkitCommandSource src, @Named("tag") String tag) {
    }
}
