package dev.velix.imperat;

import dev.velix.imperat.adventure.AdventureProvider;
import dev.velix.imperat.adventure.CastingAdventure;
import dev.velix.imperat.adventure.EmptyAdventure;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.exception.UnknownOfflinePlayerException;
import dev.velix.imperat.exception.UnknownPlayerException;
import dev.velix.imperat.exception.UnknownWorldException;
import dev.velix.imperat.selector.TargetSelector;
import dev.velix.imperat.type.ParameterOfflinePlayer;
import dev.velix.imperat.type.ParameterPlayer;
import dev.velix.imperat.type.ParameterTargetSelector;
import dev.velix.imperat.type.ParameterWorld;
import dev.velix.imperat.util.reflection.Reflections;
import net.kyori.adventure.audience.Audience;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BukkitConfigBuilder extends ConfigBuilder<BukkitSource, BukkitImperat> {

    private final static BukkitPermissionResolver DEFAULT_PERMISSION_RESOLVER = new BukkitPermissionResolver();

    private final Plugin plugin;
    private final AdventureProvider<CommandSender> adventureProvider;

    private boolean supportBrigadier = false;

    BukkitConfigBuilder(Plugin plugin, @Nullable AdventureProvider<CommandSender> adventureProvider) {
        this.plugin = plugin;
        this.adventureProvider = adventureProvider;

        config.setPermissionResolver(DEFAULT_PERMISSION_RESOLVER);
        addThrowableHandlers();
        registerSourceResolvers();
        registerValueResolvers();
    }

    private void registerSourceResolvers() {
        config.registerSourceResolver(CommandSender.class, BukkitSource::origin);
        config.registerSourceResolver(Player.class, (source) -> {
            if (source.isConsole()) {
                throw new SourceException("Only players are allowed to do this !");
            }
            return source.asPlayer();
        });
    }

    private void addThrowableHandlers() {
        config.setThrowableResolver(
            UnknownPlayerException.class, (exception, imperat, context) ->
                context.source().error("A player with the name '" + exception.getName() + "' doesn't seem to be online")
        );
        config.setThrowableResolver(
            UnknownOfflinePlayerException.class, (exception, imperat, context) ->
                context.source().error("A player with the name '" + exception.getName() + "' doesn't seem to exist")
        );
        config.setThrowableResolver(
            UnknownWorldException.class, (exception, imperat, context) ->
                context.source().error("A world with the name '" + exception.getName() + "' doesn't seem to exist")
        );
    }

    private void registerValueResolvers() {
        config.registerParamType(Player.class, new ParameterPlayer());
        config.registerParamType(OfflinePlayer.class, new ParameterOfflinePlayer());
        config.registerParamType(TargetSelector.class, new ParameterTargetSelector());
        var worldClass = Reflections.getClass("org.bukkit.World");
        config.registerParamType(worldClass, new ParameterWorld(worldClass));
    }

    public static BukkitConfigBuilder builder(Plugin plugin) {
        return builder(plugin, null);
    }

    public static BukkitConfigBuilder builder(Plugin plugin, @Nullable AdventureProvider<CommandSender> adventureProvider) {
        return new BukkitConfigBuilder(plugin, loadAdventure(plugin, adventureProvider));
    }

    public BukkitConfigBuilder applyBrigadier(boolean supportBrigadier) {
        this.supportBrigadier = supportBrigadier;
        return this;
    }

    @Override
    public @NotNull BukkitImperat build() {
        return new BukkitImperat(plugin, adventureProvider, supportBrigadier, this.config);
    }

    private static AdventureProvider<CommandSender> loadAdventure(Plugin plugin, @Nullable AdventureProvider<CommandSender> provider) {
        if (Reflections.findClass("net.kyori.adventure.audience.Audience")) {
            if (provider != null) {
                return provider;
            } else if (Audience.class.isAssignableFrom(CommandSender.class)) {
                return new CastingAdventure<>();
            } else if (Reflections.findClass("net.kyori.adventure.platform.bukkit.BukkitAudiences")) {
                return new BukkitAdventure(plugin);
            }
        }

        return new EmptyAdventure<>();
    }

}
