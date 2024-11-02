package dev.velix.imperat;

import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.exception.UnknownPlayerException;
import net.minestom.server.ServerProcess;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class MinestomConfigBuilder extends ConfigBuilder<MinestomSource, MinestomImperat> {

    private final ServerProcess serverProcess;

    private MinestomConfigBuilder(@NotNull ServerProcess serverProcess) {
        this.serverProcess = serverProcess;
        registerDefaultResolvers();
        addThrowableHandlers();
    }

    private void registerDefaultResolvers() {
        config.registerSourceResolver(CommandSender.class, MinestomSource::origin);

        config.registerSourceResolver(Player.class, source -> {
            if (source.isConsole()) {
                throw new SourceException("Only players are allowed to do this!");
            }
            return source.asPlayer();
        });
    }

    private void addThrowableHandlers() {
        config.setThrowableResolver(
            UnknownPlayerException.class, (exception, imperat, context) ->
                context.source().error("A player with the name '" + exception.getName() + "' is not online.")
        );
    }

    public static MinestomConfigBuilder builder(@NotNull ServerProcess serverProcess) {
        return new MinestomConfigBuilder(serverProcess);
    }

    @Override
    public @NotNull MinestomImperat build() {
        return new MinestomImperat(serverProcess, config);
    }
}