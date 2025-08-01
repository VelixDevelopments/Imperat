package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.util.ImperatDebugger;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
final class InternalBukkitCommand extends org.bukkit.command.Command implements PluginIdentifiableCommand {

    @NotNull
    private final BukkitImperat dispatcher;

    @NotNull
    final Command<BukkitSource> imperatCommand;

    InternalBukkitCommand(
        final @NotNull BukkitImperat dispatcher,
        final @NotNull Command<BukkitSource> imperatCommand
    ) {
        super(
            imperatCommand.name(),
            imperatCommand.description().toString(),
            CommandUsage.format((String) null, imperatCommand.getDefaultUsage()),
            imperatCommand.aliases()
        );
        this.dispatcher = dispatcher;
        this.imperatCommand = imperatCommand;
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return dispatcher.getPlatform();
    }

    @Nullable
    @Override
    public String getPermission() {
        return imperatCommand.permission();
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
            dispatcher.dispatch(source, this.imperatCommand, label, raw);
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
        BukkitSource source = dispatcher.wrapSender(sender);
        try {
            return dispatcher.autoComplete(imperatCommand, source, alias, args).join();
        } catch (Exception ex) {
            ImperatDebugger.error(InternalBukkitCommand.class, "tabComplete", ex);
            return Collections.emptyList();
        }
    }



}