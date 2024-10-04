package dev.velix.imperat;

import dev.velix.imperat.adventure.AdventureProvider;
import dev.velix.imperat.adventure.BungeeAdventure;
import dev.velix.imperat.adventure.EmptyAdventure;
import dev.velix.imperat.command.BaseImperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.exception.UnknownPlayerException;
import dev.velix.imperat.resolvers.BungeePermissionResolver;
import dev.velix.imperat.resolvers.PermissionResolver;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.reflection.Reflections;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BungeeImperat extends BaseImperat<BungeeSource> {
	
	private final static BungeePermissionResolver DEFAULT_PERMISSION_RESOLVER = new BungeePermissionResolver();
	
	private final Plugin plugin;
	private final AdventureProvider<CommandSender> adventureProvider;
	
	private BungeeImperat(
		final Plugin plugin,
		final AdventureProvider<CommandSender> adventureProvider,
		final PermissionResolver<BungeeSource> permissionResolver
	) {
		super(permissionResolver);
		this.plugin = plugin;
		ImperatDebugger.setLogger(plugin.getLogger());
		this.adventureProvider = loadAdventureProvider(adventureProvider);
		
		registerSourceResolvers();
		registerValueResolvers();
		registerSuggestionResolvers();
		registerThrowableResolvers();
	}
	
	private AdventureProvider<CommandSender> loadAdventureProvider(@Nullable AdventureProvider<CommandSender> provider) {
		if (provider != null) {
			return provider;
		} else if (Reflections.findClass("net.kyori.adventure.platform.bungeecord.BungeeAudiences")) {
			return new BungeeAdventure(plugin);
		}
		return new EmptyAdventure<>();
	}
	
	private void registerSourceResolvers() {
		registerSourceResolver(CommandSender.class, BungeeSource::origin);
		this.registerSourceResolver(ProxiedPlayer.class, (source) -> {
			if (source.isConsole()) {
				throw new SourceException("Only players are allowed to do this !");
			}
			return source.asPlayer();
		});
	}
	
	private void registerValueResolvers() {
		registerValueResolver(ProxiedPlayer.class, (ctx, param, cursor, raw) -> {
			if (raw.equalsIgnoreCase("me")) {
				if (ctx.source().isConsole()) {
					throw new UnknownPlayerException(raw);
				}
				return ctx.source().asPlayer();
			}
			
			ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(raw);
			if (proxiedPlayer == null) {
				throw new UnknownPlayerException(raw);
			}
			return proxiedPlayer;
		});
	}
	
	private void registerSuggestionResolvers() {
		registerSuggestionResolver(
			SuggestionResolver.type(ProxiedPlayer.class, ProxyServer.getInstance().getPlayers()
				.stream().map(ProxiedPlayer::getName).toList())
		);
	}
	
	private void registerThrowableResolvers() {
		this.setThrowableResolver(
			UnknownPlayerException.class, (exception, imperat, context) ->
				context.source().error("A player with the name '" + exception.getName() + "' doesn't seem to be online")
		);
	}
	
	public static BungeeImperat create(
		@NotNull Plugin plugin,
		@Nullable AdventureProvider<CommandSender> audiences,
		@NotNull PermissionResolver<BungeeSource> permissionResolver
	) {
		return new BungeeImperat(plugin, audiences, permissionResolver);
	}
	
	public static BungeeImperat create(
		Plugin plugin,
		@Nullable AdventureProvider<CommandSender> audienceProvider
	) {
		return create(plugin, audienceProvider, DEFAULT_PERMISSION_RESOLVER);
	}
	
	public static BungeeImperat create(
		Plugin plugin,
		@NotNull PermissionResolver<BungeeSource> permissionResolver
	) {
		return create(plugin, null, permissionResolver);
	}
	
	public static BungeeImperat create(Plugin plugin) {
		return create(plugin, null, DEFAULT_PERMISSION_RESOLVER);
	}
	
	@Override
	public void registerCommand(Command<BungeeSource> command) {
		super.registerCommand(command);
		plugin.getProxy().getPluginManager().registerCommand(plugin, new InternalBungeeCommand(this, command));
	}
	
	@Override
	public String commandPrefix() {
		return "/";
	}
	
	@Override
	public BungeeSource wrapSender(Object sender) {
		return new BungeeSource(adventureProvider, (CommandSender) sender);
	}
	
	@Override
	public Plugin getPlatform() {
		return plugin;
	}
	
	@Override
	public void shutdownPlatform() {
		this.adventureProvider.close();
		this.plugin.onDisable();
	}
	
}
