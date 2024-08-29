package dev.velix.imperat;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents the sender/source
 * of a command being executed
 * may be a console, a player in a game, etc...
 *
 * @param <O> the command source type
 */
@ApiStatus.AvailableSince("1.0.0")
public interface Source<O> {

    /**
     * @return name of a command source
     */
    String getName();

    /**
     * @return The original command sender type instance
     */
    O getOrigin();

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

    @SuppressWarnings("unchecked")
    default <T> T as(Class<T> clazz) {
        return (T) this.getOrigin();
    }

}
