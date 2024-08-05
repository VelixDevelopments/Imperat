package dev.velix.imperat.command;

import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.util.StringUtils;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
final class FlagUsageParameter extends InputParameter {


	FlagUsageParameter(String flagName) {
		super(flagName, CommandFlag.class, false, true, true, false, null);
	}

	@Override
	public <C> String format(Command<C> command) {
		CommandFlag commandFlag = command.getKnownFlags().getData(this.getName())
				  .orElse(null);
		if(commandFlag == null) return "N/A";
		return StringUtils.normalizedParameterFormatting(
				  "-" + commandFlag.alias(), isOptional()
		);
	}
}
