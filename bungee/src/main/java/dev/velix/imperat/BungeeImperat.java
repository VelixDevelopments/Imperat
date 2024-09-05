package dev.velix.imperat;

import dev.velix.imperat.adventure.AdventureProvider;
import dev.velix.imperat.adventure.BungeeAdventure;
import dev.velix.imperat.adventure.NoAdventure;
import dev.velix.imperat.command.BaseImperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.resolvers.BungeePermissionResolver;
import dev.velix.imperat.resolvers.PermissionResolver;
import dev.velix.imperat.util.reflection.Reflections;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BungeeImperat extends BaseImperat<BungeeSource> {
    
    private final static BungeePermissionResolver DEFAULT_PERMISSION_RESOLVER = new BungeePermissionResolver();
    
    private final Plugin plugin;
    private final AdventureProvider<CommandSender> provider;
    
    private BungeeImperat(
            final Plugin plugin,
            final AdventureProvider<CommandSender> provider,
            final PermissionResolver<BungeeSource> permissionResolver
    ) {
        super(permissionResolver);
        this.plugin = plugin;
        if (provider != null) {
            this.provider = provider;
        } else if (Reflections.findClass("net.kyori.adventure.platform.bungeecord.BungeeAudiences")) {
            this.provider = new BungeeAdventure(plugin);
        } else {
            this.provider = new NoAdventure<>();
        }
    }
    
    public static BungeeImperat create(
            @NotNull Plugin plugin,
            @Nullable AdventureProvider<CommandSender> audiences,
            @NotNull PermissionResolver<BungeeSource> permissionResolver
    ) {
        return new BungeeImperat(plugin, audiences, permissionResolver);
    }
    
    public static BungeeImperat create(
            Plugin plugin,
            @Nullable AdventureProvider<CommandSender> audienceProvider
    ) {
        return create(plugin, audienceProvider, DEFAULT_PERMISSION_RESOLVER);
    }
    
    public static BungeeImperat create(
            Plugin plugin,
            @NotNull PermissionResolver<BungeeSource> permissionResolver
    ) {
        return create(plugin, null, permissionResolver);
    }
    
    public static BungeeImperat create(Plugin plugin) {
        return create(plugin, null, DEFAULT_PERMISSION_RESOLVER);
    }
    
    @Override
    public void registerCommand(Command<BungeeSource> command) {
        super.registerCommand(command);
        plugin.getProxy().getPluginManager().registerCommand(plugin, new InternalBungeeCommand(this, command));
    }
    
    @Override
    public String commandPrefix() {
        return "/";
    }
    
    @Override
    public BungeeSource wrapSender(Object sender) {
        return new BungeeSource(this, provider, (CommandSender) sender);
    }
    
    @Override
    public Object getPlatform() {
        return plugin;
    }
    
    
    @Override
    public void shutdownPlatform() {
        this.provider.close();
        this.plugin.onDisable();
    }
    
}
