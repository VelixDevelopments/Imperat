package dev.velix.imperat;

import dev.velix.imperat.adventure.AdventureProvider;
import dev.velix.imperat.brigadier.BukkitBrigadierManager;
import dev.velix.imperat.command.BaseImperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.util.BukkitUtil;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class BukkitImperat extends BaseImperat<BukkitSource> {

    private final Plugin plugin;
    private final AdventureProvider<CommandSender> adventureProvider;
    private BukkitBrigadierManager brigadierManager;
    private Map<String, org.bukkit.command.Command> bukkitCommands = new HashMap<>();

    public static BukkitConfigBuilder builder(Plugin plugin) {
        return new BukkitConfigBuilder(plugin);
    }

    @SuppressWarnings("unchecked")
    BukkitImperat(Plugin plugin, AdventureProvider<CommandSender> adventureProvider, boolean supportBrigadier, ImperatConfig<BukkitSource> config) {
        super(config);
        this.plugin = plugin;
        this.adventureProvider = adventureProvider;

        ImperatDebugger.setLogger(plugin.getLogger());
        try {
            if (BukkitUtil.KNOWN_COMMANDS != null) {
                this.bukkitCommands = (Map<String, org.bukkit.command.Command>)
                    BukkitUtil.KNOWN_COMMANDS.get(BukkitUtil.COMMAND_MAP);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (supportBrigadier) {
            applyBrigadier();
        }

    }

    /**
     * Wraps the sender into a built-in command-sender valueType
     *
     * @param sender the sender's actual value
     * @return the wrapped command-sender valueType
     */
    @Override
    public BukkitSource wrapSender(Object sender) {
        return new BukkitSource((CommandSender) sender, adventureProvider);
    }

    /**
     * @return the platform of the module
     */
    @Override
    public Plugin getPlatform() {
        return plugin;
    }

    @Override
    public void shutdownPlatform() {
        this.adventureProvider.close();
        Bukkit.getPluginManager().disablePlugin(plugin);
    }

    /**
     * Registering a command into the dispatcher
     *
     * @param command the command to register
     */
    @Override
    public void registerCommand(Command<BukkitSource> command) {
        super.registerCommand(command);
        var internalCmd = WrappedBukkitCommand.wrap(command, new InternalBukkitCommand(this, command));

        BukkitUtil.COMMAND_MAP.register(this.plugin.getName(), internalCmd);

        if (brigadierManager != null) {
            brigadierManager.registerBukkitCommand(internalCmd, command, config.getPermissionResolver());
        }
    }

    /**
     * Unregisters a command from the internal registry
     *
     * @param name the name of the command to unregister
     */
    @Override
    public void unregisterCommand(String name) {
        Command<BukkitSource> imperatCmd = getCommand(name);
        super.unregisterCommand(name);

        if (imperatCmd == null) return;
        for (var entry : new HashSet<>(bukkitCommands.entrySet())) {
            var originalKey = entry.getKey();
            var key = StringUtils.stripNamespace(originalKey);

            if (imperatCmd.hasName(key)) {
                bukkitCommands.remove(originalKey);
            }
        }
        try {
            if (BukkitUtil.KNOWN_COMMANDS != null) {
                BukkitUtil.KNOWN_COMMANDS.set(BukkitUtil.COMMAND_MAP, bukkitCommands);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Unregisters all commands from the internal registry
     */
    @Override
    public void unregisterAllCommands() {
        super.unregisterAllCommands();
        if (BukkitUtil.KNOWN_COMMANDS != null) {
            bukkitCommands.clear();
            try {
                BukkitUtil.KNOWN_COMMANDS.set(BukkitUtil.COMMAND_MAP, bukkitCommands);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        //BukkitUtil.COMMAND_MAP.clearCommands();
    }

    public void applyBrigadier() {
        brigadierManager = BukkitBrigadierManager.load(this);
    }

}
