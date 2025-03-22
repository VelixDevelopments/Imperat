package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.util.ImperatDebugger;
import org.bukkit.command.CommandException;
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
            CommandUsage.format(command, command.getDefaultUsage()),
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
            dispatcher.dispatch(source, this.command, raw);
            return true;
        } catch (Exception ex) {
            ImperatDebugger.error(InternalBukkitCommand.class, "execute", ex);
            return false;
        }

    }

    public @NotNull List<String> tabComplete0(
        final @NotNull CommandSender sender,
        final String[] args
    ) throws IllegalArgumentException {
        //ImperatDebugger.debug("Starting bukkit tab-completion");
        BukkitSource source = dispatcher.wrapSender(sender);
        try {
            //ImperatDebugger.debug("Size of completions= %s", completions.size());
            List<String> completions = dispatcher.autoComplete(command, source, args).join();
            ImperatDebugger.debug("Completions= " + String.join(",", completions));
            return completions;
        } catch (Exception ex) {
            ImperatDebugger.error(InternalBukkitCommand.class, "tabComplete", ex);
            return Collections.emptyList();
        }
    }


    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws CommandException, IllegalArgumentException {
        List<String> completions;

        try {
            completions = this.tabComplete0(sender, args);

        } catch (Throwable ex) {
            StringBuilder message = new StringBuilder();
            message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');

            for(String arg : args) {
                message.append(arg).append(' ');
            }

            message.deleteCharAt(message.length() - 1).append("' in plugin ").append(this.getPlugin().getDescription().getFullName());
            throw new CommandException(message.toString(), ex);
        }

        return completions.isEmpty() ? super.tabComplete(sender, alias, args) : completions;
    }


}