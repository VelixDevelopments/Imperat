package dev.velix.imperat;

import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.context.Context;
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
     * This message is parsed using minimessage format for platforms
     * that can integrate with Kyori-Adventure chat-components.
     *
     * @param message the message
     */
    void reply(String message);
    
    /**
     * Replies to the command sender with a caption message
     * @param caption the {@link Caption} to send
     * @param context the {@link Context} to use
     */
    void reply(Caption<O> caption, Context<O> context);
    
    /**
     * Replies to command sender with a caption message
     * @param prefix the prefix before the caption message
     * @param caption the caption
     * @param context the context
     */
    void reply(String prefix, Caption<O> caption, Context<O> context);
    
    /**
     * @return Whether the command source is from the console
     */
    boolean isConsole();

    @SuppressWarnings("unchecked")
    default <T> T as(Class<T> clazz) {
        return (T) this.getOrigin();
    }

}
