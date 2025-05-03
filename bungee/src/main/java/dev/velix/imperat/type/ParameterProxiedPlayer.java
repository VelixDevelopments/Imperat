package dev.velix.imperat.type;

import dev.velix.imperat.BungeeSource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.UnknownPlayerException;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.TypeWrap;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public final class ParameterProxiedPlayer extends BaseParameterType<BungeeSource, ProxiedPlayer> {

    private final ProxiedPlayerSuggestionResolver PROXIED_PLAYER_SUGGESTION_RESOLVER = new ProxiedPlayerSuggestionResolver();

    public ParameterProxiedPlayer() {
        super(TypeWrap.of(ProxiedPlayer.class));
    }

    @Override
    public @NotNull ProxiedPlayer resolve(
            @NotNull ExecutionContext<BungeeSource> context,
            @NotNull CommandInputStream<BungeeSource> commandInputStream,
            String input) throws ImperatException {

        if (input.equalsIgnoreCase("me")) {
            if (context.source().isConsole()) {
                throw new UnknownPlayerException(input);
            }
            return context.source().asPlayer();
        }

        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(input);
        if (proxiedPlayer == null) {
            throw new UnknownPlayerException(input);
        }
        return proxiedPlayer;
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<BungeeSource> parameter) {
        return ProxyServer.getInstance().getPlayer(input) != null;
    }

    /**
     * Returns the suggestion resolver associated with this parameter type.
     *
     * @return the suggestion resolver for generating suggestions based on the parameter type.
     */
    @Override
    public SuggestionResolver<BungeeSource> getSuggestionResolver() {
        return PROXIED_PLAYER_SUGGESTION_RESOLVER;
    }

    private final static class ProxiedPlayerSuggestionResolver implements SuggestionResolver<BungeeSource> {

        /**
         * @param context   the context for suggestions
         * @param parameter the parameter of the value to complete
         * @return the auto-completed suggestions of the current argument
         */
        @Override
        public List<String> autoComplete(SuggestionContext<BungeeSource> context, CommandParameter<BungeeSource> parameter) {
            return ProxyServer.getInstance().getPlayers().stream()
                .map(ProxiedPlayer::getName)
                .toList();
        }
    }

}
