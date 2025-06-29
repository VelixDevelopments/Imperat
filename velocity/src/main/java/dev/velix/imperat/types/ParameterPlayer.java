package dev.velix.imperat.types;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.velix.imperat.VelocitySource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.UnknownPlayerException;
import dev.velix.imperat.resolvers.SuggestionResolver;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public final class ParameterPlayer extends BaseParameterType<VelocitySource, Player> {

    private final ProxyServer proxyServer;
    private final PlayerSuggestionResolver playerSuggestionResolver;

    public ParameterPlayer(ProxyServer server) {
        super();
        this.proxyServer = server;
        this.playerSuggestionResolver = new PlayerSuggestionResolver(server);
    }

    @Override
    public @NotNull Player resolve(
            @NotNull ExecutionContext<VelocitySource> context,
            @NotNull CommandInputStream<VelocitySource> commandInputStream,
            @NotNull String input) throws ImperatException {

        if (input.equalsIgnoreCase("me")) {
            if (context.source().isConsole()) {
                throw new UnknownPlayerException(input);
            }
            return context.source().asPlayer();
        }
        return proxyServer.getPlayer(input.toLowerCase()).orElseThrow(() -> new UnknownPlayerException(input));
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<VelocitySource> parameter) {
        return input.length() < 16;
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
