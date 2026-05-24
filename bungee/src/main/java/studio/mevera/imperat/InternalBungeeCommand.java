package studio.mevera.imperat;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.TabExecutor;
import studio.mevera.imperat.command.Command;


final class InternalBungeeCommand<S extends BungeeCommandSource> extends net.md_5.bungee.api.plugin.Command implements TabExecutor {

    private final BungeeImperat<S> bungeeCommandDispatcher;
    private final Command<S> bungeeCommand;

    InternalBungeeCommand(
            BungeeImperat<S> commandDispatcher,
            Command<S> bungeeCommand
    ) {
        super(
                bungeeCommand.getName(),
                bungeeCommand.getPrimaryPermission(),
                bungeeCommand.aliases().toArray(new String[0])
        );
        this.bungeeCommandDispatcher = commandDispatcher;
        this.bungeeCommand = bungeeCommand;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        bungeeCommandDispatcher.execute(
                bungeeCommandDispatcher.wrapSender(sender),
                bungeeCommand.getName(),
                args
        );
    }


    @Override
    public Iterable<String> onTabComplete(
            CommandSender sender,
            String[] args
    ) {
        final String input = this.bungeeCommand.getName() + " " + String.join(" ", args);
        return bungeeCommandDispatcher.autoComplete(
                bungeeCommandDispatcher.wrapSender(sender),
                input
        ).join();
    }

}
