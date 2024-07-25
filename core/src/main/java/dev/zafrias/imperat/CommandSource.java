package dev.zafrias.imperat;

import net.kyori.adventure.text.Component;

/**
 * Represents the sender/source
 * of a command being executed
 * may be a console, a player in a game, etc...
 *
 * @param <C> the command source type
 */
public interface CommandSource<C> {

	/**
	 * @return The original command sender type instance
	 */
	C getOrigin();

	/**
	 * Replies to the command sender with a string message
	 * this message is auto translated into a minimessage
	 *
	 * @param message the message
	 */
	void reply(String message);


	/**
	 * Replies to the command sender with a chat component
	 *
	 * @param component the chat component
	 */
	void reply(Component component);

	/**
	 * @return Whether the command source is from the console
	 */
	boolean isConsole();

}
