package dev.velix.imperat.context;

import dev.velix.imperat.caption.Caption;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents the sender/source
 * of a command being executed
 * may be a console, a player in a game, etc...
 */
@ApiStatus.AvailableSince("1.0.0")
public interface Source {

    /**
     * @return name of a command source
     */
    String name();

    /**
     * @return The original command sender type instance
     */
    Object origin();

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
     *
     * @param caption the {@link Caption} to send
     * @param context the {@link Context} to use
     */
    <S extends Source> void reply(Caption<S> caption, Context<S> context);

    /**
     * Replies to command sender with a caption message
     *
     * @param prefix  the prefix before the caption message
     * @param caption the caption
     * @param context the context
     */
    <S extends Source> void reply(String prefix, Caption<S> caption, Context<S> context);

    /**
     * @return Whether the command source is from the console
     */
    boolean isConsole();

    @SuppressWarnings("unchecked")
    default <T> T as(Class<T> clazz) {
        return (T) this.origin();
    }

}
