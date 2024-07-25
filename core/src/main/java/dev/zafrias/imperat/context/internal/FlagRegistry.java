package dev.zafrias.imperat.context.flags.internal;

import dev.zafrias.imperat.context.flags.CommandFlag;
import dev.zafrias.imperat.util.Registry;
import java.util.Optional;
import java.util.stream.Collector;

public final class FlagRegistry extends Registry<String, CommandFlag> {

	static Collector<CommandFlag, FlagRegistry, FlagRegistry> COLLECTOR = Collector.of(
			  FlagRegistry::new,
			  (flagRegistry, commandFlag) -> flagRegistry.setData(commandFlag.getName(), commandFlag),
			  (left, right)-> (FlagRegistry) left.addAll(right)
	);

	public FlagRegistry() {
		super();
	}

	public Optional<CommandFlag> searchFlagAlias(String flagAlias) {
		return search((name, flag)-> flag.hasAlias(flagAlias));
	}
}
