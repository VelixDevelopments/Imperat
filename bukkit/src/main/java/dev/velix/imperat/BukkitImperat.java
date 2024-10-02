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
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.Preconditions;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.reflection.Reflections;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Map;

public final class BukkitImperat extends BaseImperat<BukkitSource> {

    private final static BukkitPermissionResolver DEFAULT_PERMISSION_RESOLVER = new BukkitPermissionResolver();
    private final Plugin plugin;
    private final AdventureProvider<CommandSender> adventureProvider;
    private Map<String, org.bukkit.command.Command> bukkitOGMapping;
    private BukkitBrigadierManager brigadierManager;

    @SuppressWarnings("unchecked")
    private BukkitImperat(Plugin plugin, AdventureProvider<CommandSender> adventureProvider, @NotNull PermissionResolver<BukkitSource> permissionResolver) {
        super(permissionResolver);
        this.plugin = plugin;
        ImperatDebugger.setLogger(plugin.getLogger());
        if (BukkitUtil.KNOWN_COMMANDS != null) {
            try {
                bukkitOGMapping = (Map<String, org.bukkit.command.Command>) BukkitUtil.KNOWN_COMMANDS.get(BukkitUtil.COMMAND_MAP);
            } catch (IllegalAccessException e) {
                ImperatDebugger.warning("Failed to access the internal command map");
            }
        }
        this.addThrowableHandlers();
        this.adventureProvider = loadAdventure(adventureProvider);
        registerSourceResolvers();
        registerValueResolvers();
        registerSuggestionResolvers();
        
        
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

    @SuppressWarnings("unchecked")
    public static Class<? extends Entity> getSelectedEntity(@NotNull Type selectorType) {
        return (Class<? extends Entity>) TypeUtility.getInsideGeneric(selectorType, Entity.class);
    }

    /**
     * Wraps the sender into a built-in command-sender type
     *
     * @param sender the sender's actual value
     * @return the wrapped command-sender type
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
        if (BukkitUtil.KNOWN_COMMANDS != null) {
            bukkitOGMapping.put(command.name(), internalCmd);
        } else {
            BukkitUtil.COMMAND_MAP.register(command.name(), internalCmd);
        }
        if (brigadierManager != null) {
            brigadierManager.registerBukkitCommand(internalCmd, command, permissionResolver);
        }
    }

    private void registerValueResolvers() {
        this.registerValueResolver(Player.class, (context, parameter, cursor, raw) -> {
            if (raw.equalsIgnoreCase("me")) {
                if (context.source().isConsole()) {
                    throw new UnknownPlayerException(raw);
                }
                return context.source().asPlayer();
            }
            final Player player = Bukkit.getPlayer(raw.toLowerCase());
            if (player != null) return player;
            throw new UnknownPlayerException(raw);
        });

        this.registerValueResolver(OfflinePlayer.class, (context, parameter, cursor, raw) -> {
            if (raw.length() > 16) {
                throw new UnknownPlayerException(raw);
            }
            return Bukkit.getOfflinePlayer(raw);
        });

        this.registerValueResolver(World.class, (context, parameter, cursor, raw) -> {
            World world = Bukkit.getWorld(raw.toLowerCase());
            if (world != null) return world;
            throw new UnknownWorldException(raw);
        });
    }

    private void registerSuggestionResolvers() {
        registerSuggestionResolver(BukkitSuggestionResolvers.PLAYER);
        registerSuggestionResolver(BukkitSuggestionResolvers.OFFLINE_PLAYER);
    }

    public void applyBrigadier() {
        brigadierManager = BukkitBrigadierManager.load(this);
        //TODO apply on all currently registered commands
    }

}
