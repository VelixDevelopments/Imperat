package dev.velix.imperat;

import dev.velix.imperat.adventure.AdventureProvider;
import dev.velix.imperat.adventure.BungeeAdventure;
import dev.velix.imperat.adventure.NoAdventure;
import dev.velix.imperat.command.BaseImperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.resolvers.BungeePermissionResolver;
import dev.velix.imperat.resolvers.PermissionResolver;
import dev.velix.imperat.util.reflection.Reflections;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BungeeImperat extends BaseImperat<CommandSender> {

    private final static BungeePermissionResolver DEFAULT_PERMISSION_RESOLVER = new BungeePermissionResolver();

    private final Plugin plugin;
    private final AdventureProvider<CommandSender> provider;

    private BungeeImperat(
            final Plugin plugin,
            final AdventureProvider<CommandSender> provider,
            final PermissionResolver<CommandSender> permissionResolver
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
            @NotNull PermissionResolver<CommandSender> permissionResolver
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
            @NotNull PermissionResolver<CommandSender> permissionResolver
    ) {
        return create(plugin, null, permissionResolver);
    }

    public static BungeeImperat create(Plugin plugin) {
        return create(plugin, null, DEFAULT_PERMISSION_RESOLVER);
    }

    @Override
    public void registerCommand(Command<CommandSender> command) {
        super.registerCommand(command);
        plugin.getProxy().getPluginManager().registerCommand(plugin, new InternalBungeeCommand(this, command));
    }

    @Override
    public String commandPrefix() {
        return "/";
    }

    @Override
    public Source<CommandSender> wrapSender(CommandSender sender) {
        return new BungeeSource(this, provider, sender);
    }

    @Override
    public Object getPlatform() {
        return plugin;
    }

    @Override
    public CommandHelp<CommandSender> createCommandHelp(
            Command<CommandSender> command,
            Context<CommandSender> context
    ) {
        return BungeeCommandHelp.create(this, command, context);
    }

    @Override
    public void shutdownPlatform() {
        this.provider.close();
        this.plugin.onDisable();
    }

}
