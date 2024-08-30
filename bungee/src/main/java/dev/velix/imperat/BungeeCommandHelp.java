package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.help.CommandHelp;
import net.md_5.bungee.api.CommandSender;

public final class BungeeCommandHelp extends CommandHelp<CommandSender> {
    private BungeeCommandHelp(
            Imperat<CommandSender> dispatcher,
            Command<CommandSender> command,
            Context<CommandSender> context
    ) {
        super(dispatcher, command, context);
    }

    public static BungeeCommandHelp create(
            BungeeImperat dispatcher,
            Command<CommandSender> command,
            Context<CommandSender> context
    ) {
        return new BungeeCommandHelp(dispatcher, command, context);
    }

}
