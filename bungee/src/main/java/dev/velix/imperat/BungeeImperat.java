package dev.velix.imperat;

import dev.velix.imperat.adventure.AdventureProvider;
import dev.velix.imperat.command.BaseImperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.StringUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public final class BungeeImperat extends BaseImperat<BungeeSource> {

    private final Plugin plugin;
    private final AdventureProvider<CommandSender> adventureProvider;

    BungeeImperat(
        Plugin plugin,
        @NotNull AdventureProvider<CommandSender> adventureProvider,
        ImperatConfig<BungeeSource> config
    ) {
        super(config);
        this.plugin = plugin;
        this.adventureProvider = adventureProvider;
        ImperatDebugger.setLogger(plugin.getLogger());
    }

    @Override
    public void registerCommand(Command<BungeeSource> command) {
        super.registerCommand(command);
        plugin.getProxy().getPluginManager().registerCommand(plugin, new InternalBungeeCommand(this, command));
    }

    @Override
    public void unregisterCommand(String name) {
        Command<BungeeSource> imperatCmd = getCommand(name);
        super.unregisterCommand(name);
        if (imperatCmd == null) return;

        for (var entry : new HashSet<>(plugin.getProxy().getPluginManager().getCommands())) {
            var key = StringUtils.stripNamespace(entry.getKey());

            if (imperatCmd.hasName(key)) {
                plugin.getProxy().getPluginManager().unregisterCommand(entry.getValue());
            }
        }
    }

    @Override
    public BungeeSource wrapSender(Object sender) {
        return new BungeeSource(adventureProvider, (CommandSender) sender);
    }

    @Override
    public Plugin getPlatform() {
        return plugin;
    }

    @Override
    public void shutdownPlatform() {
        this.adventureProvider.close();
        this.plugin.onDisable();
    }
}
