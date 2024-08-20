package dev.velix.imperat;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BukkitCommandSource implements CommandSource<CommandSender> {
	private final CommandSender sender;
	private final AudienceProvider audiences;
	
	public BukkitCommandSource(CommandSender sender, AudienceProvider audiences) {
		this.sender = sender;
		this.audiences = audiences;
	}
	
	/**
	 * @return name of command source
	 */
	@Override
	public String getName() {
		return sender.getName();
	}
	
	/**
	 * @return The original command sender type instance
	 */
	@Override
	public CommandSender getOrigin() {
		return sender;
	}
	
	public Player asPlayer() {
		return as(Player.class);
	}
	
	/**
	 * Replies to the command sender with a string message
	 * this message is auto translated into a minimessage
	 *
	 * @param message the message
	 */
	@Override
	public void reply(String message) {
		sender.sendMessage(message);
	}
	
	/**
	 * Replies to the command sender with a chat component
	 *
	 * @param component the chat component
	 */
	@Override
	public void reply(Component component) {
		Audience audience = isConsole() ? audiences.console()
						: audiences.player(((Player) sender).getUniqueId());
		audience.sendMessage(component);
	}
	
	/**
	 * @return Whether the command source is from the console
	 */
	@Override
	public boolean isConsole() {
		return !(sender instanceof Player);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T as(Class<T> clazz) {
		return (T) getOrigin();
	}
	
	
}
