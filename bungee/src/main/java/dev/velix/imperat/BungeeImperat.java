package dev.velix.imperat;

import dev.velix.imperat.adventure.AdventureProvider;
import dev.velix.imperat.adventure.BungeeAdventure;
import dev.velix.imperat.adventure.EmptyAdventure;
import dev.velix.imperat.command.BaseImperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.StringUtils;
import dev.velix.imperat.util.reflection.Reflections;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashSet;

public final class BungeeImperat extends BaseImperat<BungeeSource> {

    private final Plugin plugin;
    private final AdventureProvider<CommandSender> adventureProvider;

    BungeeImperat(
        Plugin plugin,
        AdventureProvider<CommandSender> adventureProvider,
        ImperatConfig<BungeeSource> config
    ) {
        super(config);
        this.plugin = plugin;
        ImperatDebugger.setLogger(plugin.getLogger());
        this.adventureProvider = adventureProvider != null ? adventureProvider : loadAdventureProvider();
    }

    private AdventureProvider<CommandSender> loadAdventureProvider() {
        if (Reflections.findClass("net.kyori.adventure.platform.bungeecord.BungeeAudiences")) {
            return new BungeeAdventure(plugin);
        }
        return new EmptyAdventure<>();
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
