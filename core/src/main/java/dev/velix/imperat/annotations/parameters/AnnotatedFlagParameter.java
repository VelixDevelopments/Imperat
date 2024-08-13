package dev.velix.imperat.annotations.parameters;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.util.StringUtils;
import dev.velix.imperat.util.annotations.AnnotationMap;

public final class AnnotatedFlagParameter extends AnnotatedInputParameter{
	AnnotatedFlagParameter(String name,
	                       AnnotationMap map) {
		super(name, CommandFlag.class, true,
				  true, false, null, map);
	}

	/**
	 * Formats the usage parameter
	 * using the command
	 *
	 * @param command The command owning this parameter
	 * @return the formatted parameter
	 */
	@Override
	public <C> String format(Command<C> command) {
		CommandFlag commandFlag = command.getKnownFlags().getData(this.getName())
				  .orElse(null);
		if (commandFlag == null) return "N/A";
		return StringUtils.normalizedParameterFormatting(
				  "-" + commandFlag.alias(), isOptional()
		);
	}
}
