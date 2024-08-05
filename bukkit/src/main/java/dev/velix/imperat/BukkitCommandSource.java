package dev.velix.imperat;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BukkitCommandSource implements CommandSource<CommandSender> {
	private final CommandSender sender;
	private final BukkitAudiences audiences;
	public BukkitCommandSource(CommandSender sender, BukkitAudiences audiences) {
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
		//TODO implement
		audiences.sender(sender)
				  .sendMessage(component);
	}

	/**
	 * @return Whether the command source is from the console
	 */
	@Override
	public boolean isConsole() {
		return !(sender instanceof Player);
	}
}
