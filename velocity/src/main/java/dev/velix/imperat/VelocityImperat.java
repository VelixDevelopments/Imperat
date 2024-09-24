package dev.velix.imperat;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.velix.imperat.command.BaseImperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.exception.UnknownPlayerException;
import dev.velix.imperat.resolvers.PermissionResolver;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.Preconditions;
import org.jetbrains.annotations.NotNull;

public final class VelocityImperat extends BaseImperat<VelocitySource> {
    
    private final PluginContainer plugin;
    private final ProxyServer proxyServer;
    
    private final BaseBrigadierManager<VelocitySource> velocityBrigadier;
    
    private VelocityImperat(
            @NotNull String pluginName,
            @NotNull ProxyServer proxyServer,
            @NotNull PermissionResolver<VelocitySource> permissionResolver
    ) {
        super(permissionResolver);
        Preconditions.notEmpty(pluginName, "plugin id/name is not valid");
        this.plugin = proxyServer.getPluginManager().getPlugin(pluginName).orElseThrow(() -> new IllegalStateException("Unknown plugin with name '" + pluginName + "'"));
        this.proxyServer = proxyServer;
        this.velocityBrigadier = VelocityBrigadier.create(this);

        this.registerDefaultResolvers();
    }
    
    public static VelocityImperat create(
            String pluginName,
            ProxyServer server,
            @NotNull PermissionResolver<VelocitySource> permissionResolver
    ) {
        return new VelocityImperat(pluginName, server, permissionResolver);
    }
    
    public static VelocityImperat create(
            String pluginName,
            ProxyServer proxyServer
    ) {
        return create(pluginName, proxyServer, (source, permission) -> source.origin().hasPermission(permission));
    }

    private void registerDefaultResolvers() {
        // Player
        this.registerValueResolver(Player.class, (context, parameter, cursor, raw) -> {
            if (raw.equalsIgnoreCase("me")) {
                if (context.source().isConsole()) {
                    throw new UnknownPlayerException(raw);
                }
                return context.source().asPlayer();
            }
            return proxyServer.getPlayer(raw.toLowerCase()).orElseThrow(() -> new UnknownPlayerException(raw));
        });

        this.registerSuggestionResolver(
                SuggestionResolver.plain(Player.class, proxyServer.getAllPlayers().stream().map(Player::getUsername).toList())
        );
    }

    @Override
    public void registerCommand(Command<VelocitySource> command) {
        super.registerCommand(command);
        CommandManager manager = proxyServer.getCommandManager();
        try {
            LiteralCommandNode<CommandSource> commandNode = velocityBrigadier.parseCommandIntoNode(command);
            
            manager.register(
                    manager.metaBuilder(command.name().toLowerCase())
                            .aliases(command.aliases().toArray(new String[0]))
                            .plugin(plugin).build(),
                    new BrigadierCommand(commandNode)
            );
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    public ProxyServer getPlatform() {
        return proxyServer;
    }
    
    @Override
    public void shutdownPlatform() {
        plugin.getExecutorService().shutdown();
    }
    
    @Override
    public String commandPrefix() {
        return "/";
    }
    
    @Override
    public VelocitySource wrapSender(Object sender) {
        return new VelocitySource((CommandSource) sender);
    }
}
