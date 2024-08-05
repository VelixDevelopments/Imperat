package dev.velix.imperat.caption;

import dev.velix.imperat.caption.premade.CooldownCaption;
import dev.velix.imperat.caption.premade.InvalidSyntaxCaption;
import dev.velix.imperat.caption.premade.NoPermissionCaption;
import dev.velix.imperat.util.Registry;
import org.jetbrains.annotations.Nullable;

public final class CaptionRegistry<C> extends Registry<CaptionKey, Caption<C>> {

	public CaptionRegistry() {
		super();
		this.registerCaption(new NoPermissionCaption<>());
		this.registerCaption(new CooldownCaption<>());
		this.registerCaption(new InvalidSyntaxCaption<>());
	}

	public @Nullable Caption<C> getCaption(CaptionKey key) {
		return getData(key).orElse(null);
	}

	public void registerCaption(Caption<C> caption) {
		this.setData(caption.getKey(), caption);
	}

}
