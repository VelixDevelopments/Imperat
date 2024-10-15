package dev.velix.imperat.brigadier;

import dev.velix.imperat.BaseBrigadierManager;
import dev.velix.imperat.BukkitImperat;
import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.WrappedBukkitCommand;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.commodore.Commodore;
import dev.velix.imperat.commodore.CommodoreProvider;
import dev.velix.imperat.resolvers.PermissionResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import static dev.velix.imperat.commodore.CommodoreProvider.isSupported;

public final class BukkitBrigadierManager extends BaseBrigadierManager<BukkitSource> {

    private final Commodore<WrappedBukkitCommand> commodore;

    public BukkitBrigadierManager(BukkitImperat dispatcher) {
        super(dispatcher);
        this.commodore = CommodoreProvider.getCommodore(dispatcher.getPlatform());
        if (isSupported()) {
            registerArgumentResolver(String.class, DefaultArgTypeResolvers.STRING);
            registerArgumentResolver(DefaultArgTypeResolvers.NUMERIC);
            registerArgumentResolver(DefaultArgTypeResolvers.ENTITY_SELECTOR);
            registerArgumentResolver(Boolean.class, DefaultArgTypeResolvers.BOOLEAN);
            registerArgumentResolver(Player.class, DefaultArgTypeResolvers.PLAYER);
            registerArgumentResolver(OfflinePlayer.class, DefaultArgTypeResolvers.PLAYER);
        }
    }

    public static @Nullable BukkitBrigadierManager load(BukkitImperat bukkitCommandDispatcher) {
        if (!isSupported()) {
            return null;
        }
        return new BukkitBrigadierManager(bukkitCommandDispatcher);
    }

    @Override
    public BukkitSource wrapCommandSource(Object commandSource) {
        return dispatcher.wrapSender(commodore.wrapNMSCommandSource(commandSource));
    }

    public void registerBukkitCommand(
        WrappedBukkitCommand bukkitCmd,
        Command<BukkitSource> imperatCommand,
        PermissionResolver<BukkitSource> resolver
    ) {
        commodore.register(bukkitCmd, parseCommandIntoNode(imperatCommand),
            (player) -> resolver.hasPermission(wrapCommandSource(player), bukkitCmd.permission()));
    }
}
