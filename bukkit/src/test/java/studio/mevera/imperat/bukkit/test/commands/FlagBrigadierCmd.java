package studio.mevera.imperat.bukkit.test.commands;

import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.annotations.types.Execute;
import studio.mevera.imperat.annotations.types.Flag;
import studio.mevera.imperat.annotations.types.Greedy;
import studio.mevera.imperat.annotations.types.Named;
import studio.mevera.imperat.annotations.types.RootCommand;
import studio.mevera.imperat.annotations.types.SubCommand;
import studio.mevera.imperat.annotations.types.Suggest;
import studio.mevera.imperat.annotations.types.Switch;

@RootCommand("flagtest")
public final class FlagBrigadierCmd {

    @Execute
    public void root(
            BukkitCommandSource source,
            @Flag({"scenario", "sc"})
            @Suggest({"kindergarten", "castle", "sandstorm", "tsunami"})
            String scenario
    ) {
    }

    @SubCommand("play")
    public void play(
            BukkitCommandSource source,
            @Flag({"scenario", "sc"})
            @Suggest({"kindergarten", "castle", "sandstorm", "tsunami"})
            String scenario
    ) {
    }

    @SubCommand("mix")
    public void mix(
            BukkitCommandSource source,
            String target,
            @Flag({"scenario", "sc"})
            @Suggest({"kindergarten", "castle", "sandstorm", "tsunami"})
            String scenario
    ) {
    }

    @SubCommand("greedyflag")
    public void greedyflag(
            BukkitCommandSource source,
            @Named("target") @Greedy @Suggest({"pluginsDir/itsmyconfig/", "pluginsDir/my plugin/config.yml", "pluginsDir/my plugin/messages.yml"}) String target,
            @Switch("shallow") boolean shallow
    ) {
    }
}
