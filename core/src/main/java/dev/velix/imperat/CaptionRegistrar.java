package dev.velix.imperat;

import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.caption.CaptionKey;
import dev.velix.imperat.context.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface CaptionRegistrar<C> permits Imperat {
	
	/**
	 * Registers a caption
	 *
	 * @param caption the caption to register
	 */
	void registerCaption(Caption<C> caption);
	
	
	/**
	 * Sends a caption to the source
	 *
	 * @param context   the context of the command
	 * @param exception the error
	 * @param key       the id/key of the caption
	 */
	void sendCaption(
					CaptionKey key,
					Context<C> context,
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
					Context<C> context
	);
	
	/**
	 * Fetches the caption from a caption key
	 * @param key the key
	 * @return the caption to get
	 */
	@Nullable Caption<C> getCaption(@NotNull CaptionKey key);
	
}
