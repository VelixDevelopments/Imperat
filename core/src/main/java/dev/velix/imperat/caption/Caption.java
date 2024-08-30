package dev.velix.imperat.caption;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Context;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a message
 */
@ApiStatus.AvailableSince("1.0.0")
public interface Caption<C> {

    /**
     * @return the key
     */
    @NotNull
    CaptionKey getKey();

    /**
     * @param dispatcher the command dispatcher
     * @param context    the context
     * @param exception  the exception may be null if no exception provided
     * @return The message in the form of a component
     */
    @NotNull
    String getMessage(
            @NotNull Imperat<C> dispatcher,
            @NotNull Context<C> context,
            @Nullable Exception exception
    );


    default @NotNull String getMessage(
            @NotNull Imperat<C> dispatcher,
            @NotNull Context<C> context
    ) {
        return getMessage(dispatcher, context, null);
    }

}
