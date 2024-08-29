package dev.velix.imperat;

import dev.velix.imperat.command.BaseCommandDispatcher;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.resolvers.BungeePermissionResolver;
import dev.velix.imperat.resolvers.PermissionResolver;
import net.kyori.adventure.platform.AudienceProvider;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BungeeCommandDispatcher extends BaseCommandDispatcher<CommandSender> {
	
	private final static BungeePermissionResolver DEFAULT_PERMISSION_RESOLVER = new BungeePermissionResolver();
	
	private final Plugin plugin;
	private final AudienceProvider provider;
	private BungeeCommandDispatcher(Plugin plugin,
																	AudienceProvider provider,
	                                PermissionResolver<CommandSender> permissionResolver) {
		super(permissionResolver);
		this.plugin = plugin;
		this.provider = provider;
	}
	
	
	public static BungeeCommandDispatcher create(
					@NotNull Plugin plugin,
					@Nullable AudienceProvider audiences,
					@NotNull PermissionResolver<CommandSender> permissionResolver
	) {
		return new BungeeCommandDispatcher(plugin, audiences, permissionResolver);
	}
	
	public static BungeeCommandDispatcher create(
					Plugin plugin,
					@Nullable AudienceProvider audienceProvider
	) {
		return create(plugin, audienceProvider, DEFAULT_PERMISSION_RESOLVER);
	}
	
	public static BungeeCommandDispatcher create(
					Plugin plugin,
					@NotNull PermissionResolver<CommandSender> permissionResolver
	) {
		return create(plugin, null, permissionResolver);
	}
	
	public static BungeeCommandDispatcher create(Plugin plugin) {
		return create(plugin, null, DEFAULT_PERMISSION_RESOLVER);
	}
	
	@Override
	public void registerCommand(Command<CommandSender> command) {
		super.registerCommand(command);
		plugin.getProxy().getPluginManager().registerCommand(plugin, new InternalBungeeCommand(this, command));
	}
	
	@Override
	public String commandPrefix() {
		return "/";
	}
	
	@Override
	public CommandSource<CommandSender> wrapSender(CommandSender sender) {
		return new BungeeCommandSource(provider, sender);
	}
	
	@Override
	public Object getPlatform() {
		return plugin;
	}
	
	@Override
	public CommandHelp<CommandSender> createCommandHelp(
					Command<CommandSender> command,
					Context<CommandSender> context,
					CommandUsage<CommandSender> detectedUsage
	) {
		return BungeeCommandHelp.create(this, command, context, detectedUsage);
	}
	
	@Override
	public void shutdownPlatform() {
		plugin.onDisable();
	}
}
