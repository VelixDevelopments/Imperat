package studio.mevera.imperat.bukkit.test.commands;

import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.annotations.types.Flag;
import studio.mevera.imperat.annotations.types.Greedy;
import studio.mevera.imperat.annotations.types.Named;
import studio.mevera.imperat.annotations.types.RootCommand;
import studio.mevera.imperat.annotations.types.SubCommand;
import studio.mevera.imperat.annotations.types.Suggest;

@RootCommand("chat")
public final class GreedyAliasBrigadierCmd {

    @SubCommand({"send", "s"})
    public void send(
            BukkitCommandSource source,
            @Flag({"scenario", "sc"})
            @Suggest({"kindergarten", "castle"})
            String scenario,
            @Named("message") @Greedy String message
    ) {
    }
}
