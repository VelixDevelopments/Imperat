package dev.velix.imperat;

import dev.velix.imperat.caption.Messages;
import dev.velix.imperat.command.AbstractCommandDispatcher;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.exceptions.context.ContextResolveException;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.resolvers.PermissionResolver;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class BukkitCommandDispatcher extends AbstractCommandDispatcher<CommandSender> {
	
	private final Plugin plugin;
	private final Map<String, Command> commandMap;
	private final AudienceProvider provider;
	
	private final static LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
					.character('&')
					.hexColors()
					.hexCharacter('#')
					.build();
	
	private BukkitCommandDispatcher(Plugin plugin, AudienceProvider audienceProvider) {
		super();
		this.plugin = plugin;
		this.provider = audienceProvider == null ? BukkitAudiences.create(plugin) : audienceProvider;
		try {
			commandMap = BukkitUtil.getCommandMap();
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		registerValueResolvers();
	}
	
	public static BukkitCommandDispatcher create(Plugin plugin, @Nullable AudienceProvider audiences) {
		RegisteredServiceProvider<BukkitCommandDispatcher> provider =
						Bukkit.getServicesManager().getRegistration(BukkitCommandDispatcher.class);
		if (provider == null) {
			BukkitCommandDispatcher dispatcher = new BukkitCommandDispatcher(plugin, audiences);
			Bukkit.getServicesManager().register(BukkitCommandDispatcher.class, dispatcher, plugin, ServicePriority.Normal);
			return dispatcher;
		}
		return provider.getProvider();
	}
	
	public static BukkitCommandDispatcher create(Plugin plugin) {
		return create(plugin, null);
	}
	
	/**
	 * Wraps the sender into a built-in command-sender type
	 *
	 * @param sender the sender's actual value
	 * @return the wrapped command-sender type
	 */
	@Override
	public CommandSource<CommandSender> wrapSender(CommandSender sender) {
		return new BukkitCommandSource(sender, provider);
	}
	
	/**
	 * @return the platform of the module
	 */
	@Override
	public Object getPlatform() {
		return plugin;
	}
	
	/**
	 * Creates an instance of {@link CommandHelp}
	 *
	 * @param command       the command
	 * @param context       the context
	 * @param detectedUsage the usage
	 * @return {@link CommandHelp} for the command usage used in a certain context
	 */
	@Override
	public CommandHelp<CommandSender> createCommandHelp(
					dev.velix.imperat.command.Command<CommandSender> command,
					Context<CommandSender> context,
					CommandUsage<CommandSender> detectedUsage
	) {
		return BukkitCommandHelp.create(this, command, context, detectedUsage);
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
	
	private void registerValueResolvers() {
		
		this.registerValueResolver(Player.class, ((source, context, raw) -> {
			Player player = Bukkit.getPlayer(raw.toLowerCase());
			if (player != null) return player;
			throw new ContextResolveException("Player '" + raw + "' is offline or invalid");
		}));
		
		this.registerValueResolver(World.class, (source, context, raw) -> {
			World world = Bukkit.getWorld(raw.toLowerCase());
			if (world != null) return world;
			throw new ContextResolveException("Invalid world '" + raw + "'");
		});
		
		registerValueResolver(Component.class, (source, context, raw) -> {
			String result = Messages.MINI_MESSAGE.stripTags(raw);
			if (result.length() == raw.length()) {
				return Messages.getMsg(raw);
			}
			return LEGACY_COMPONENT_SERIALIZER.deserialize(raw);
		});
	}
	
}
