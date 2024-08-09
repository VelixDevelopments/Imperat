package dev.velix.imperat.command.parameters;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.resolvers.OptionalValueSupplier;
import dev.velix.imperat.util.StringUtils;

final class NormalUsageParameter extends InputParameter {

	NormalUsageParameter(String name,
	                     Class<?> type,
	                     boolean optional,
	                     boolean greedy,
	                     OptionalValueSupplier<?, ?> valueSupplier) {
		super(name, type, optional, false, greedy, valueSupplier);
	}

	/**
	 * Formats the usage parameter
	 *
	 * @return the formatted parameter
	 */
	@Override
	public <C> String format(Command<C> command) {
		var content = getName();
		if(isGreedy())
			content += "...";
		return StringUtils.normalizedParameterFormatting(content, isOptional());
	}
}
