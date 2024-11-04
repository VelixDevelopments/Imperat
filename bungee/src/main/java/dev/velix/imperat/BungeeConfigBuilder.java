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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BungeeConfigBuilder extends ConfigBuilder<BungeeSource, BungeeImperat> {

    private final static BungeePermissionResolver DEFAULT_PERMISSION_RESOLVER = new BungeePermissionResolver();
    private final Plugin plugin;
    private final AdventureProvider<CommandSender> adventureProvider;

    private BungeeConfigBuilder(Plugin plugin, @Nullable AdventureProvider<CommandSender> adventureProvider) {
        this.plugin = plugin;
        this.adventureProvider = adventureProvider != null ? adventureProvider : loadAdventureProvider();
        config.setPermissionResolver(DEFAULT_PERMISSION_RESOLVER);
        addThrowableHandlers();
        registerSourceResolvers();
        registerValueResolvers();
    }

    private AdventureProvider<CommandSender> loadAdventureProvider() {
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

    public static BungeeConfigBuilder builder(Plugin plugin) {
        return builder(plugin, null);
    }

    public static BungeeConfigBuilder builder(Plugin plugin, @Nullable AdventureProvider<CommandSender> adventureProvider) {
        return new BungeeConfigBuilder(plugin, adventureProvider);
    }

    @Override
    public @NotNull BungeeImperat build() {
        return new BungeeImperat(plugin, adventureProvider, this.config);
    }
}