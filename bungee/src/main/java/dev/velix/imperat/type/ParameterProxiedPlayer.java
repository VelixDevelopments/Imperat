package dev.velix.imperat.type;

import dev.velix.imperat.BungeeSource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.UnknownPlayerException;
import dev.velix.imperat.util.TypeWrap;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class ParameterProxiedPlayer extends BaseParameterType<BungeeSource, ProxiedPlayer> {

    public ParameterProxiedPlayer() {
        super(TypeWrap.of(ProxiedPlayer.class));
    }

    @Override
    public @Nullable ProxiedPlayer resolve(
        ExecutionContext<BungeeSource> context,
        @NotNull CommandInputStream<BungeeSource> commandInputStream
    ) throws ImperatException {
        String currentRaw = commandInputStream.currentRaw().orElse(null);
        if (currentRaw == null) return null;

        if (currentRaw.equalsIgnoreCase("me")) {
            if (context.source().isConsole()) {
                throw new UnknownPlayerException(currentRaw);
            }
            return context.source().asPlayer();
        }

        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(currentRaw);
        if (proxiedPlayer == null) {
            throw new UnknownPlayerException(currentRaw);
        }
        return proxiedPlayer;
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<BungeeSource> parameter) {
        return ProxyServer.getInstance().getPlayer(input) != null;
    }

    @Override
    public Collection<String> suggestions() {
        return ProxyServer.getInstance().getPlayers()
            .stream().map(ProxiedPlayer::getName).toList();
    }
}
