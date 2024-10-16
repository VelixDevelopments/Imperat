package dev.velix.imperat.types;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.velix.imperat.VelocitySource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.UnknownPlayerException;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class ParameterPlayer extends BaseParameterType<VelocitySource, Player> {

    private final ProxyServer proxyServer;

    public ParameterPlayer(ProxyServer server) {
        super(TypeWrap.of(Player.class));
        this.proxyServer = server;
    }

    @Override
    public @Nullable Player resolve(
        ExecutionContext<VelocitySource> context,
        @NotNull CommandInputStream<VelocitySource> commandInputStream
    ) throws ImperatException {
        String currentRaw = commandInputStream.currentRaw().orElse(null);
        if (currentRaw == null) return null;

        if (currentRaw.equalsIgnoreCase("me")) {
            if (context.source().isConsole()) {
                throw new UnknownPlayerException(currentRaw);
            }
            return context.source().asPlayer();
        }
        return proxyServer.getPlayer(currentRaw.toLowerCase()).orElseThrow(() -> new UnknownPlayerException(currentRaw));
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<VelocitySource> parameter) {
        return proxyServer.getPlayer(input).isPresent();
    }

    @Override
    public Collection<String> suggestions() {
        return proxyServer.getAllPlayers().stream().map(Player::getUsername).toList();
    }
}
