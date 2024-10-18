package dev.velix.imperat;

import dev.velix.imperat.adventure.AdventureProvider;
import dev.velix.imperat.adventure.CastingAdventure;
import dev.velix.imperat.adventure.EmptyAdventure;
import dev.velix.imperat.brigadier.BukkitBrigadierManager;
import dev.velix.imperat.command.BaseImperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.exception.UnknownOfflinePlayerException;
import dev.velix.imperat.exception.UnknownPlayerException;
import dev.velix.imperat.exception.UnknownWorldException;
import dev.velix.imperat.resolvers.PermissionResolver;
import dev.velix.imperat.type.ParameterOfflinePlayer;
import dev.velix.imperat.type.ParameterPlayer;
import dev.velix.imperat.type.ParameterWorld;
import dev.velix.imperat.util.BukkitUtil;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.Preconditions;
import dev.velix.imperat.util.StringUtils;
import dev.velix.imperat.util.reflection.Reflections;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class BukkitImperat extends BaseImperat<BukkitSource> {

    private final static BukkitPermissionResolver DEFAULT_PERMISSION_RESOLVER = new BukkitPermissionResolver();
    private final Plugin plugin;
    private final AdventureProvider<CommandSender> adventureProvider;
    private BukkitBrigadierManager brigadierManager;
    private Map<String, org.bukkit.command.Command> bukkitCommands = new HashMap<>();

    @SuppressWarnings("unchecked")
    private BukkitImperat(Plugin plugin, AdventureProvider<CommandSender> adventureProvider, @NotNull PermissionResolver<BukkitSource> permissionResolver) {
        super(permissionResolver);
        this.plugin = plugin;
        ImperatDebugger.setLogger(plugin.getLogger());
        try {
            if (BukkitUtil.KNOWN_COMMANDS != null) {
                this.bukkitCommands = (Map<String, org.bukkit.command.Command>)
                    BukkitUtil.KNOWN_COMMANDS.get(BukkitUtil.COMMAND_MAP);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        this.addThrowableHandlers();
        this.adventureProvider = loadAdventure(adventureProvider);
        registerSourceResolvers();
        registerValueResolvers();
    }

    private void registerSourceResolvers() {
        this.registerSourceResolver(CommandSender.class, BukkitSource::origin);
        this.registerSourceResolver(Player.class, (source) -> {
            if (source.isConsole()) {
                throw new SourceException("Only players are allowed to do this !");
            }
            return source.asPlayer();
        });
    }

    private void addThrowableHandlers() {
        this.setThrowableResolver(
            UnknownPlayerException.class, (exception, imperat, context) ->
                context.source().error("A player with the name '" + exception.getName() + "' doesn't seem to be online")
        );
        this.setThrowableResolver(
            UnknownOfflinePlayerException.class, (exception, imperat, context) ->
                context.source().error("A player with the name '" + exception.getName() + "' doesn't seem to exist")
        );
        this.setThrowableResolver(
            UnknownWorldException.class, (exception, imperat, context) ->
                context.source().error("A world with the name '" + exception.getName() + "' doesn't seem to exist")
        );

    }

    private AdventureProvider<CommandSender> loadAdventure(@Nullable AdventureProvider<CommandSender> provider) {
        if (Reflections.findClass("net.kyori.adventure.audience.Audience")) {
            if (provider != null) {
                return provider;
            } else if (Audience.class.isAssignableFrom(CommandSender.class)) {
                return new CastingAdventure<>();
            } else if (Reflections.findClass("net.kyori.adventure.platform.bukkit.BukkitAudiences")) {
                return new BukkitAdventure(plugin);
            }
        }

        return new EmptyAdventure<>();
    }

    /**
     * Creates a bukkit command dispatcher instance
     *
     * @param plugin             the plugin
     * @param audienceProvider   the kyori adventure audience provider
     * @param permissionResolver the permission resolver
     * @return the new or existing bukkit command dispatcher instance in the bukkit server
     */
    public static BukkitImperat create(
        @NotNull Plugin plugin,
        @Nullable AdventureProvider<CommandSender> audienceProvider,
        @NotNull PermissionResolver<BukkitSource> permissionResolver
    ) {
        Preconditions.notNull(plugin, "plugin");
        Preconditions.notNull(permissionResolver, "permission-resolver");
        return new BukkitImperat(plugin, audienceProvider, permissionResolver);
    }

    public static BukkitImperat create(Plugin plugin, @Nullable AdventureProvider<CommandSender> audienceProvider) {
        return create(plugin, audienceProvider, DEFAULT_PERMISSION_RESOLVER);
    }

    public static BukkitImperat create(Plugin plugin, @NotNull PermissionResolver<BukkitSource> permissionResolver) {
        return create(plugin, null, permissionResolver);
    }

    public static BukkitImperat create(Plugin plugin) {
        return create(plugin, null, DEFAULT_PERMISSION_RESOLVER);
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

    @Override
    public String commandPrefix() {
        return "/";
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
            brigadierManager.registerBukkitCommand(internalCmd, command, permissionResolver);
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

    private void registerValueResolvers() {
        this.registerParamType(Player.class, new ParameterPlayer());
        this.registerParamType(OfflinePlayer.class, new ParameterOfflinePlayer());
        this.registerParamType(Reflections.getClass("org.bukkit.World"), new ParameterWorld());
    }

    public void applyBrigadier() {
        brigadierManager = BukkitBrigadierManager.load(this);
    }

}
