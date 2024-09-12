package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.help.CommandHelp;

public final class BukkitHelp extends CommandHelp<BukkitSource> {
    
    private BukkitHelp(Imperat<BukkitSource> dispatcher, Command<BukkitSource> command, Context<BukkitSource> context) {
        super(dispatcher, command, context);
    }
    
    public static BukkitHelp create(Imperat<BukkitSource> source, Command<BukkitSource> command, Context<BukkitSource> context) {
        return new BukkitHelp(source, command, context);
    }
    
}
