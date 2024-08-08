package dev.velix.imperat.command.parameters;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.util.StringUtils;

final class NormalUsageParameter extends InputParameter {

	NormalUsageParameter(String name,
	                     Class<?> type,
	                     boolean optional,
	                     boolean greedy,
	                     Object defaultValue) {
		super(name, type, optional, false, greedy, defaultValue);
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
