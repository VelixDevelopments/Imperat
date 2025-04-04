package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.util.ImperatDebugger;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.*;
import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
final class InternalBukkitCommand extends org.bukkit.command.Command implements PluginIdentifiableCommand {

    @NotNull
    private final BukkitImperat dispatcher;

    @NotNull
    private final Command<BukkitSource> command;

    InternalBukkitCommand(
        final @NotNull BukkitImperat dispatcher,
        final @NotNull Command<BukkitSource> command
    ) {
        super(
            command.name(),
            command.description().toString(),
            CommandUsage.format(null, command.getDefaultUsage()),
            command.aliases()
        );
        this.dispatcher = dispatcher;
        this.command = command;
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return dispatcher.getPlatform();
    }

    @Nullable
    @Override
    public String getPermission() {
        return command.permission();
    }

    @NotNull
    @Override
    public String getDescription() {
        return super.getDescription();
    }

    @NotNull
    @Override
    public String getUsage() {
        return super.getUsage();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender,
                           @NotNull String label,
                           String[] raw) {

        try {
            BukkitSource source = dispatcher.wrapSender(sender);
            dispatcher.dispatch(source, this.command, label, raw);
            return true;
        } catch (Exception ex) {
            ImperatDebugger.error(InternalBukkitCommand.class, "execute", ex);
            return false;
        }

    }

    @Override
    public @NotNull List<String> tabComplete(
        final @NotNull CommandSender sender,
        final @NotNull String alias,
        final String[] args
    ) throws IllegalArgumentException {
        //ImperatDebugger.debug("Starting bukkit tab-completion");
        BukkitSource source = dispatcher.wrapSender(sender);
        try {
            //ImperatDebugger.debug("Size of completions= %s", completions.size());
            return dispatcher.autoComplete(command, source, alias, args).join();
        } catch (Exception ex) {
            ImperatDebugger.error(InternalBukkitCommand.class, "tabComplete", ex);
            return Collections.emptyList();
        }
    }



}