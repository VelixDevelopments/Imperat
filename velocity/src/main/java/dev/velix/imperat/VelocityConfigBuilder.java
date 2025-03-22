package dev.velix.imperat;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.exception.UnknownPlayerException;
import dev.velix.imperat.types.ParameterPlayer;
import org.jetbrains.annotations.*;

public final class VelocityConfigBuilder extends ConfigBuilder<VelocitySource, VelocityImperat> {

    private final PluginContainer plugin;
    private final ProxyServer proxyServer;

    VelocityConfigBuilder(@NotNull PluginContainer plugin, @NotNull ProxyServer proxyServer) {
        this.plugin = plugin;
        this.proxyServer = proxyServer;
        addThrowableHandlers();
        registerSourceResolvers();
        registerValueResolvers();
    }

    private void registerSourceResolvers() {
        config.registerSourceResolver(CommandSource.class, VelocitySource::origin);

        config.registerSourceResolver(Player.class, (source) -> {
            if (source.isConsole()) {
                throw new SourceException("Only players are allowed to do this!");
            }
            return source.asPlayer();
        });
    }

    private void addThrowableHandlers() {
        config.setThrowableResolver(
            UnknownPlayerException.class, (exception, imperat, context) ->
                context.source().error("A player with the name '" + exception.getName() + "' doesn't seem to be online")
        );
    }

    private void registerValueResolvers() {
        config.registerParamType(Player.class, new ParameterPlayer(proxyServer));
    }

    @Override
    public @NotNull VelocityImperat build() {
        return new VelocityImperat(plugin, proxyServer, this.config);
    }
}