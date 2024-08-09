package dev.velix.imperat;

import dev.velix.imperat.command.AbstractCommandDispatcher;
import dev.velix.imperat.resolvers.PermissionResolver;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import java.util.Map;

public final class BukkitCommandDispatcher extends AbstractCommandDispatcher<CommandSender> {

	private final Plugin plugin;
	private final Map<String, Command> commandMap;
	private final BukkitAudiences audiences;

	private BukkitCommandDispatcher(Plugin plugin) {
		super();
		this.plugin = plugin;
		this.audiences = BukkitAudiences.create(plugin);
		try {
			commandMap = BukkitUtil.getCommandMap();
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static BukkitCommandDispatcher create(Plugin plugin) {
		RegisteredServiceProvider<BukkitCommandDispatcher> provider =
				  Bukkit.getServicesManager().getRegistration(BukkitCommandDispatcher.class);
		if (provider == null) {
			BukkitCommandDispatcher dispatcher = new BukkitCommandDispatcher(plugin);
			Bukkit.getServicesManager().register(BukkitCommandDispatcher.class, dispatcher, plugin, ServicePriority.Normal);
			return dispatcher;
		}
		return provider.getProvider();
	}

	/**
	 * Wraps the sender into a built-in command-sender type
	 *
	 * @param sender the sender's actual value
	 * @return the wrapped command-sender type
	 */
	@Override
	public CommandSource<CommandSender> wrapSender(CommandSender sender) {
		return new BukkitCommandSource(sender, audiences);
	}

	/**
	 * @return the platform of the module
	 */
	@Override
	public Object getPlatform() {
		return plugin;
	}


	@Override
	public String commandPrefix() {
		return "/";
	}

	/**
	 * Registering a command into the dispatcher
	 *
	 * @param command the command to register
	 */
	@Override
	public void registerCommand(dev.velix.imperat.command.Command<CommandSender> command) {
		super.registerCommand(command);
		commandMap.put(command.getName(), new InternalBukkitCommand(this, command));
	}

	/**
	 * @return {@link PermissionResolver} for the dispatcher
	 */
	@Override
	public PermissionResolver<CommandSender> getPermissionResolver() {
		return ((source, permission) -> {
			if (permission == null) return true;
			CommandSender sender = source.getOrigin();
			return sender.hasPermission(permission);
		});
	}

}
