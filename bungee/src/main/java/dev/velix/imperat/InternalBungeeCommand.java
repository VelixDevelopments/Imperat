package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.TabExecutor;


final class InternalBungeeCommand extends net.md_5.bungee.api.plugin.Command implements TabExecutor {

    private final BungeeImperat bungeeCommandDispatcher;
    private final Command<CommandSender> bungeeCommand;

    InternalBungeeCommand(
            BungeeImperat commandDispatcher,
            Command<CommandSender> bungeeCommand
    ) {
        super(
                bungeeCommand.getName(),
                bungeeCommand.getPermission(),
                bungeeCommand.getAliases().toArray(new String[0])
        );
        this.bungeeCommandDispatcher = commandDispatcher;
        this.bungeeCommand = bungeeCommand;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        bungeeCommandDispatcher.dispatch(sender, bungeeCommand.getName(), args);
    }


    @Override
    public Iterable<String> onTabComplete(
            CommandSender sender,
            String[] args
    ) {
        return bungeeCommandDispatcher.autoComplete(bungeeCommand, sender, args);
    }

}
