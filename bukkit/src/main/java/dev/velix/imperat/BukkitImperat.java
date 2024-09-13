package dev.velix.imperat;

import dev.velix.imperat.adventure.AdventureProvider;
import dev.velix.imperat.adventure.BukkitAdventure;
import dev.velix.imperat.adventure.CastingAdventure;
import dev.velix.imperat.brigadier.BukkitBrigadierManager;
import dev.velix.imperat.command.BaseImperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.exception.SenderErrorException;
import dev.velix.imperat.resolvers.BukkitPermissionResolver;
import dev.velix.imperat.resolvers.PermissionResolver;
import dev.velix.imperat.util.CommandDebugger;
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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

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
        CommandDebugger.setLogger(plugin.getLogger());
        if (BukkitUtil.KNOWN_COMMANDS != null) {
            try {
                bukkitOGMapping = (Map<String, org.bukkit.command.Command>) BukkitUtil.KNOWN_COMMANDS.get(BukkitUtil.COMMAND_MAP);
            } catch (IllegalAccessException e) {
                CommandDebugger.warning("Failed to access the internal command map");
            }
        }
        //cleaner
        this.adventureProvider = loadAdventure(adventureProvider);
        registerValueResolvers();
        registerSuggestionResolvers();
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
        
        return BukkitAdventure.EMPTY;
    }
    
    /**
     * Creates a bukkit command dispatcher instance
     *
     * @param plugin             the plugin
     * @param audienceProvider   the kyori adventure audience provider
     * @param permissionResolver the permission resolver
     * @param override           whether to override the existing instance in the bukkit server
     *                           or just use an already existing instance in the server loaded
     *                           from any other plugin
     * @return the new or existing bukkit command dispatcher instance in the bukkit server
     */
    public static BukkitImperat create(
            @NotNull Plugin plugin,
            @Nullable AdventureProvider<CommandSender> audienceProvider,
            @NotNull PermissionResolver<BukkitSource> permissionResolver,
            boolean override
    ) {
        Preconditions.notNull(plugin, "plugin cannot be null !");
        RegisteredServiceProvider<BukkitImperat> provider =
                Bukkit.getServicesManager().getRegistration(BukkitImperat.class);
        if (provider == null || override) {
            BukkitImperat dispatcher = new BukkitImperat(plugin, audienceProvider, permissionResolver);
            Bukkit.getServicesManager().register(BukkitImperat.class, dispatcher, plugin, ServicePriority.Normal);
            return dispatcher;
        }
        return provider.getProvider();
    }
    
    public static BukkitImperat create(
            @NotNull Plugin plugin,
            @Nullable AdventureProvider<CommandSender> audienceProvider,
            @NotNull PermissionResolver<BukkitSource> permissionResolver
    ) {
        return create(plugin, audienceProvider, permissionResolver, false);
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
        var internalCmd = new InternalBukkitCommand(this, command);
        if (BukkitUtil.KNOWN_COMMANDS != null) {
            bukkitOGMapping.put(command.getName(), internalCmd);
        } else {
            BukkitUtil.COMMAND_MAP.register(command.getName(), internalCmd);
        }
        if (brigadierManager != null) {
            brigadierManager.registerBukkitCommand(internalCmd, command);
        }
    }
    
    @SuppressWarnings("deprecation")
    private void registerValueResolvers() {
        this.registerValueResolver(Player.class, ((source, context, raw, pivot, parameter) -> {
            final Player player = Bukkit.getPlayer(raw.toLowerCase());
            if (player != null) return player;
            throw new SenderErrorException("A player with the name '" + raw + "' doesn't seem to be online");
        }));
        
        this.registerValueResolver(OfflinePlayer.class, (source, context, raw, pivot, parameter) -> {
            if (raw.length() > 16) {
                throw new SenderErrorException("Invalid or Unknown offline player '%s'", raw);
            }
            return Bukkit.getOfflinePlayer(raw);
        });
        
        this.registerValueResolver(World.class, (source, context, raw, pivot, parameter) -> {
            World world = Bukkit.getWorld(raw.toLowerCase());
            if (world != null) return world;
            throw new SenderErrorException("Unknown world '%s'", raw);
        });
        
        registerValueResolver(UUID.class, (source, context, raw, pivot, parameter) -> {
            try {
                return UUID.fromString(raw);
            } catch (IllegalArgumentException ex) {
                throw new SenderErrorException("Invalid uuid-format '%s'", raw);
            }
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
