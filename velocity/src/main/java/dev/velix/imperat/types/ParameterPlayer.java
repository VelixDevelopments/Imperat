package dev.velix.imperat.types;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.velix.imperat.Imperat;
import dev.velix.imperat.VelocitySource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.UnknownPlayerException;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ParameterPlayer extends BaseParameterType<VelocitySource, Player> {

    private final ProxyServer proxyServer;
    private final PlayerSuggestionResolver playerSuggestionResolver;

    public ParameterPlayer(ProxyServer server) {
        super(TypeWrap.of(Player.class));
        this.proxyServer = server;
        this.playerSuggestionResolver = new PlayerSuggestionResolver(server);
    }

    @Override
    public @Nullable Player resolve(
            @NotNull ExecutionContext<VelocitySource> context,
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
        return input.length() < 16;
    }

    @Override
    public @NotNull Player fromString(Imperat<VelocitySource> imperat, String input) throws ImperatException {
        return proxyServer.getPlayer(input).orElseThrow();
    }

    /**
     * Returns the suggestion resolver associated with this parameter type.
     *
     * @return the suggestion resolver for generating suggestions based on the parameter type.
     */
    @Override
    public SuggestionResolver<VelocitySource> getSuggestionResolver() {
        return playerSuggestionResolver;
    }

    private final static class PlayerSuggestionResolver implements SuggestionResolver<VelocitySource> {

        private final ProxyServer proxyServer;

        PlayerSuggestionResolver(ProxyServer server) {
            this.proxyServer = server;
        }

        /**
         * @param context   the context for suggestions
         * @param parameter the parameter of the value to complete
         * @return the auto-completed suggestions of the current argument
         */
        @Override
        public List<String> autoComplete(SuggestionContext<VelocitySource> context, CommandParameter<VelocitySource> parameter) {
            return proxyServer.getAllPlayers().stream().map(Player::getUsername).toList();
        }
    }
}
