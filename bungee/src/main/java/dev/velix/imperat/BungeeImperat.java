package dev.velix.imperat;

import dev.velix.imperat.command.BaseImperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.resolvers.BungeePermissionResolver;
import dev.velix.imperat.resolvers.PermissionResolver;
import net.kyori.adventure.platform.AudienceProvider;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BungeeImperat extends BaseImperat<CommandSender> {

    private final static BungeePermissionResolver DEFAULT_PERMISSION_RESOLVER = new BungeePermissionResolver();

    private final Plugin plugin;
    private final AudienceProvider provider;

    private BungeeImperat(Plugin plugin,
                          AudienceProvider provider,
                          PermissionResolver<CommandSender> permissionResolver) {
        super(permissionResolver);
        this.plugin = plugin;
        this.provider = provider;
    }


    public static BungeeImperat create(
            @NotNull Plugin plugin,
            @Nullable AudienceProvider audiences,
            @NotNull PermissionResolver<CommandSender> permissionResolver
    ) {
        return new BungeeImperat(plugin, audiences, permissionResolver);
    }

    public static BungeeImperat create(
            Plugin plugin,
            @Nullable AudienceProvider audienceProvider
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
        plugin.onDisable();
    }
}
