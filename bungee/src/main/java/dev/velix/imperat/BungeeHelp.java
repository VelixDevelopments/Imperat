package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.help.CommandHelp;

public final class BungeeHelp extends CommandHelp<BungeeSource> {
    private BungeeHelp(
            Imperat<BungeeSource> dispatcher,
            Command<BungeeSource> command,
            Context<BungeeSource> context
    ) {
        super(dispatcher, command, context);
    }
    
    public static BungeeHelp create(
            Imperat<BungeeSource> dispatcher,
            Command<BungeeSource> command,
            Context<BungeeSource> context
    ) {
        return new BungeeHelp(dispatcher, command, context);
    }
}
