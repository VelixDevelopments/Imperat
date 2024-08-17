package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ApiStatus.Internal
final class InternalBukkitCommand extends org.bukkit.command.Command implements PluginIdentifiableCommand {
	
	@NotNull
	private final BukkitCommandDispatcher dispatcher;
	
	@NotNull
	private final Command<CommandSender> command;
	
	
	InternalBukkitCommand(@NotNull BukkitCommandDispatcher dispatcher,
	                      @NotNull Command<CommandSender> command) {
		super(
						command.getName(),
						command.getDefaultUsage().getDescription(),
						CommandUsage.format(command, command.getDefaultUsage()),
						command.getAliases()
		);
		this.dispatcher = dispatcher;
		this.command = command;
	}
	
	@Override
	public boolean execute(@NotNull CommandSender sender,
	                       @NotNull String label,
	                       String[] raw) {
		
		try {
			dispatcher.dispatch(sender, label, raw);
			return true;
		} catch (Exception ex) {
			CommandDispatcher.traceError(ex);
			return false;
		}
		
	}
	
	@Override
	public @NotNull Plugin getPlugin() {
		return (Plugin) dispatcher.getPlatform();
	}
	
	@Override
	public @NotNull List<String> tabComplete(@NotNull CommandSender sender,
	                                         @NotNull String alias,
	                                         String[] args) throws IllegalArgumentException {
		return dispatcher.suggest(command, sender, args);
	}
	
}