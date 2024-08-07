package dev.velix.imperat.context.internal;

import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.util.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.stream.Collector;

@ApiStatus.Internal
public final class FlagRegistry extends Registry<String, CommandFlag> {

	static Collector<CommandFlag, FlagRegistry, FlagRegistry> COLLECTOR = Collector.of(
			  FlagRegistry::new,
			  (flagRegistry, commandFlag) -> flagRegistry.setData(commandFlag.name(), commandFlag),
			  (left, right) -> (FlagRegistry) left.addAll(right)
	);

	public FlagRegistry() {
		super();
	}

	public Optional<CommandFlag> searchFlagAlias(String flagAlias) {
		return search((name, flag) -> flag.hasAlias(flagAlias));
	}
}