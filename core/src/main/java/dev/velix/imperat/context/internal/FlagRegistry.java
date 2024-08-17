package dev.velix.imperat.context.internal;

import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.util.Registry;
import org.jetbrains.annotations.ApiStatus;
import java.util.Optional;

@ApiStatus.Internal
public final class FlagRegistry extends Registry<String, CommandFlag> {
	
	public FlagRegistry() {
		super();
	}
	
	public Optional<CommandFlag> searchFlagAlias(String flagAlias) {
		return search((name, flag) -> flag.hasAlias(flagAlias));
	}
}
