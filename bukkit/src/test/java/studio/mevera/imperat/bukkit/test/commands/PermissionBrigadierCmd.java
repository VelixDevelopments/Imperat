package studio.mevera.imperat.bukkit.test.commands;

import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.annotations.types.Execute;
import studio.mevera.imperat.annotations.types.Permission;
import studio.mevera.imperat.annotations.types.RootCommand;
import studio.mevera.imperat.annotations.types.SubCommand;

@RootCommand("permbrig")
public class PermissionBrigadierCmd {

    @Execute
    @SubCommand("open")
    public void open(BukkitCommandSource source) {
        source.reply("open");
    }

    @Execute
    @SubCommand("restricted")
    @Permission("permbrig.restricted")
    public void restricted(BukkitCommandSource source) {
        source.reply("restricted");
    }
}
