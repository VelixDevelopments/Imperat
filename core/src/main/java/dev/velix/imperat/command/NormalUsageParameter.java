package dev.velix.imperat.command;

import dev.velix.imperat.util.StringUtils;

final class NormalUsageParameter extends InputParameter {

	NormalUsageParameter(String name,
	                     Class<?> type,
	                     boolean optional,
	                     boolean greedy,
	                     Object defaultValue) {
		super(name, type, false, optional, false, greedy, defaultValue);
	}

	/**
	 * Formats the usage parameter
	 *
	 * @return the formatted parameter
	 */
	@Override
	public <C> String format(Command<C> command) {
		return StringUtils.normalizedParameterFormatting(getName(), isOptional());
	}
}
