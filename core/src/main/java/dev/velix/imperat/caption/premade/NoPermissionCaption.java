package dev.velix.imperat.caption.premade;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.caption.CaptionKey;
import dev.velix.imperat.caption.Messages;
import dev.velix.imperat.context.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NoPermissionCaption<C> implements Caption<C> {

    /**
     * @return the key
     */
    @Override
    public @NotNull CaptionKey getKey() {
        return CaptionKey.NO_PERMISSION;
    }

    /**
     * @param dispatcher the dispatcher
     * @param context    the context
     * @param exception  the exception may be null if no exception provided
     * @return The message in the form of a component
     */
    @Override
    public @NotNull String getMessage(
            @NotNull Imperat<C> dispatcher,
            @NotNull Context<C> context,
            @Nullable Exception exception
    ) {
        return Messages.NO_PERMISSION;
    }
}
