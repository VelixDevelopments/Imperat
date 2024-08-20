package dev.velix.imperat.command.parameters;

import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.context.CommandSwitch;
import org.jetbrains.annotations.NotNull;

public interface FlagParameter extends CommandParameter {
	
	/**
	 * @return The flag's data
	 */
	@NotNull CommandFlag getFlag();
	
	/**
	 * @return checks whether this parameter is a flag
	 */
	@Override
	default boolean isFlag() {
		return true;
	}
	
	default boolean isSwitch() {
		return getFlag() instanceof CommandSwitch;
	}
}
