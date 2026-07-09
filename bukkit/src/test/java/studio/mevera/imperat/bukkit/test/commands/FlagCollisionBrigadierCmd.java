package studio.mevera.imperat.bukkit.test.commands;

import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.annotations.types.Execute;
import studio.mevera.imperat.annotations.types.Flag;
import studio.mevera.imperat.annotations.types.Named;
import studio.mevera.imperat.annotations.types.RootCommand;
import studio.mevera.imperat.annotations.types.Suggest;
import studio.mevera.imperat.annotations.types.Switch;

@RootCommand("ff")
public final class FlagCollisionBrigadierCmd {

    @Execute
    public void run(
            BukkitCommandSource source,
            @Flag({"tag", "t"}) String tag,
            @Switch({"sc"}) boolean sc,
            @Named("arena") @Suggest({"kindergarten", "IstanbulPark"}) String arena
    ) {
    }
}
