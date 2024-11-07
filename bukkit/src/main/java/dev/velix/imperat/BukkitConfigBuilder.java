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
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class BukkitConfigBuilder extends ConfigBuilder<BukkitSource, BukkitImperat> {

    private final static BukkitPermissionResolver DEFAULT_PERMISSION_RESOLVER = new BukkitPermissionResolver();

    private final Plugin plugin;
    private AdventureProvider<CommandSender> adventureProvider;

    private boolean supportBrigadier = false;

    BukkitConfigBuilder(Plugin plugin) {
        this.plugin = plugin;

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

    public void setAdventureProvider(AdventureProvider<CommandSender> adventureProvider) {
        this.adventureProvider = adventureProvider;
    }

    public BukkitConfigBuilder applyBrigadier(boolean supportBrigadier) {
        this.supportBrigadier = supportBrigadier;
        return this;
    }

    @Override
    public @NotNull BukkitImperat build() {
        if (this.adventureProvider == null) {
            this.adventureProvider = this.loadAdventure();
        }
        return new BukkitImperat(plugin, adventureProvider, supportBrigadier, this.config);
    }

    @SuppressWarnings("ConstantConditions")
    private @NotNull AdventureProvider<CommandSender> loadAdventure() {
        if (Reflections.findClass(() -> Audience.class)) {
            if (Audience.class.isAssignableFrom(CommandSender.class)) {
                return new CastingAdventure<>();
            } else if (Reflections.findClass(() -> BukkitAudiences.class)) {
                return new BukkitAdventure(plugin);
            }
        }

        return new EmptyAdventure<>();
    }

}
