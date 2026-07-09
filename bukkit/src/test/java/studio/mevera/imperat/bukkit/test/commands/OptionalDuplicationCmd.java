package studio.mevera.imperat.bukkit.test.commands;

import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.annotations.types.Default;
import studio.mevera.imperat.annotations.types.Execute;
import studio.mevera.imperat.annotations.types.Named;
import studio.mevera.imperat.annotations.types.RootCommand;
import studio.mevera.imperat.annotations.types.SubCommand;

@RootCommand("dup")
public final class OptionalDuplicationCmd {

    @Execute
    public void run(
            BukkitCommandSource source,
            @Named("first") @Default("x") String first,
            @Named("second") @Default("5") int second
    ) {
    }

    @SubCommand({"sub", "sb"})
    public void sub(BukkitCommandSource source) {
    }
}
