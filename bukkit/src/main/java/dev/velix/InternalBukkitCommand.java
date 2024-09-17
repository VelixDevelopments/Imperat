package dev.velix;

import dev.velix.command.Command;
import dev.velix.command.CommandUsage;
import dev.velix.util.ImperatDebugger;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.Internal
final class InternalBukkitCommand extends org.bukkit.command.Command implements PluginIdentifiableCommand {
    
    @NotNull
    private final BukkitImperat dispatcher;
    
    @NotNull
    private final Command<BukkitSource> command;
    
    
    InternalBukkitCommand(@NotNull BukkitImperat dispatcher,
                          @NotNull Command<BukkitSource> command) {
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
    public boolean execute(@NotNull CommandSender sender,
                           @NotNull String label,
                           String[] raw) {
        
        try {
            BukkitSource source = dispatcher.wrapSender(sender);
            dispatcher.dispatch(source, label, raw);
            return true;
        } catch (Exception ex) {
            ImperatDebugger.error(InternalBukkitCommand.class, "execute", ex);
            return false;
        }
        
    }
    
    @Override
    public @NotNull Plugin getPlugin() {
        return dispatcher.getPlatform();
    }
    
    @Nullable
    @Override
    public String getPermission() {
        return super.getPermission();
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
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender,
                                             @NotNull String alias,
                                             String[] args) throws IllegalArgumentException {
        BukkitSource source = dispatcher.wrapSender(sender);
        return dispatcher.autoComplete(command, source, args);
    }
    
}