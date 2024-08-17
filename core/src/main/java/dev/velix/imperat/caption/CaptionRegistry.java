package dev.velix.imperat.caption;

import dev.velix.imperat.caption.premade.*;
import dev.velix.imperat.util.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;


/**
 * Represents a registry for holding all the messages
 * that are sent  during the command execution process in
 * all types of different scenarios
 *
 * @see CaptionKey
 * @see Caption
 */
@ApiStatus.AvailableSince("1.0.0")
public final class CaptionRegistry<C> extends Registry<CaptionKey, Caption<C>> {
	
	public CaptionRegistry() {
		super();
		this.registerCaption(new NoPermissionCaption<>());
		this.registerCaption(new CooldownCaption<>());
		this.registerCaption(new InvalidSyntaxCaption<>());
		this.registerCaption(new NoHelpCaption<>());
		this.registerCaption(new NoHelpPageCaption<>());
	}
	
	public @Nullable Caption<C> getCaption(CaptionKey key) {
		return getData(key).orElse(null);
	}
	
	public void registerCaption(Caption<C> caption) {
		this.setData(caption.getKey(), caption);
	}
	
}
