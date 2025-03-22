package dev.velix.imperat;

import dev.velix.imperat.adventure.AdventureProvider;
import dev.velix.imperat.adventure.BungeeAdventure;
import dev.velix.imperat.adventure.EmptyAdventure;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.exception.UnknownPlayerException;
import dev.velix.imperat.resolvers.BungeePermissionResolver;
import dev.velix.imperat.type.ParameterProxiedPlayer;
import dev.velix.imperat.util.reflection.Reflections;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.*;

public final class BungeeConfigBuilder extends ConfigBuilder<BungeeSource, BungeeImperat> {

    private final static BungeePermissionResolver DEFAULT_PERMISSION_RESOLVER = new BungeePermissionResolver();
    private final Plugin plugin;

    private AdventureProvider<CommandSender> adventureProvider;

    BungeeConfigBuilder(Plugin plugin, @Nullable AdventureProvider<CommandSender> adventureProvider) {
        this.plugin = plugin;
        config.setPermissionResolver(DEFAULT_PERMISSION_RESOLVER);
        addThrowableHandlers();
        registerSourceResolvers();
        registerValueResolvers();
    }

    public void setAdventureProvider(AdventureProvider<CommandSender> adventureProvider) {
        this.adventureProvider = adventureProvider;
    }

    private AdventureProvider<CommandSender> loadAdventure() {
        if (Reflections.findClass(() -> BungeeAudiences.class)) {
            return new BungeeAdventure(plugin);
        }
        return new EmptyAdventure<>();
    }

    private void registerSourceResolvers() {
        config.registerSourceResolver(CommandSender.class, BungeeSource::origin);
        config.registerSourceResolver(ProxiedPlayer.class, (source) -> {
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
        config.registerParamType(ProxiedPlayer.class, new ParameterProxiedPlayer());
    }

    @Override
    public @NotNull BungeeImperat build() {
        if (this.adventureProvider == null) {
            this.adventureProvider = this.loadAdventure();
        }
        return new BungeeImperat(plugin, adventureProvider, this.config);
    }
}