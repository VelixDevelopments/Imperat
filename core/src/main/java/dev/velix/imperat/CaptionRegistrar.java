package dev.velix.imperat;

import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.caption.CaptionKey;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface CaptionRegistrar<S extends Source> permits Imperat {

    /**
     * Registers a caption
     *
     * @param caption the caption to register
     */
    void registerCaption(Caption<S> caption);


    /**
     * Sends a caption to the source
     *
     * @param context   the context of the command
     * @param exception the error
     * @param key       the id/key of the caption
     */
    void sendCaption(
            CaptionKey key,
            Context<S> context,
            @Nullable Exception exception
    );


    /**
     * Sends a caption to the source
     *
     * @param key     the id/key of the caption
     * @param context the context of the command
     */
    void sendCaption(
            CaptionKey key,
            Context<S> context
    );

    /**
     * Fetches the caption from a caption key
     *
     * @param key the key
     * @return the caption to get
     */
    @Nullable Caption<S> getCaption(@NotNull CaptionKey key);

}
