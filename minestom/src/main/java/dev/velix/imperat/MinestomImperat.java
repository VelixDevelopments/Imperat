package dev.velix.imperat;

import dev.velix.imperat.command.BaseImperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.resolvers.PermissionResolver;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;

public final class MinestomImperat extends BaseImperat<MinestomSource> {

    private MinestomImperat(@NotNull PermissionResolver<MinestomSource> permissionResolver) {
        super(permissionResolver);
    }

    public static MinestomImperat create(@NotNull PermissionResolver<MinestomSource> permissionResolver) {
        return new MinestomImperat(permissionResolver);
    }

    public static MinestomImperat create() {
        return create((source, permission) -> {
            if (permission == null)
                return true;
            return source.origin().hasPermission(permission);
        });
    }

    /**
     * @return the platform of the module
     */
    @Override
    public ServerProcess getPlatform() {
        return MinecraftServer.process();
    }

    /**
     * Shuts down the platform
     */
    @Override
    public void shutdownPlatform() {
        MinecraftServer.getServer().stop();
    }

    /**
     * @return The command prefix
     */
    @Override
    public String commandPrefix() {
        return "/";
    }

    /**
     * Wraps the sender into a built-in command-sender valueType
     *
     * @param sender the sender's actual value
     * @return the wrapped command-sender valueType
     */
    @Override
    public MinestomSource wrapSender(Object sender) {
        if (!(sender instanceof CommandSender commandSender)) {
            throw new IllegalArgumentException("platform sender is not of valueType `" + CommandSender.class.getName() + "`");
        }
        return new MinestomSource(commandSender);
    }

    /**
     * Registering a command into the dispatcher
     *
     * @param command the command to register
     */
    @Override
    public void registerCommand(Command<MinestomSource> command) {
        super.registerCommand(command);
        MinecraftServer.getCommandManager().register(new InternalMinestomCommand(this, command));
    }

    /**
     * Unregisters a command from the internal registry
     *
     * @param name the name of the command to unregister
     */
    @Override
    public void unregisterCommand(String name) {
        super.unregisterCommand(name);
        for (var cmd : new HashSet<>(MinecraftServer.getCommandManager().getCommands())) {
            if (cmd.getName().equalsIgnoreCase(name) || List.of(cmd.getAliases()).contains(name.toLowerCase())) {
                MinecraftServer.getCommandManager().unregister(cmd);
            }
        }

    }
}
