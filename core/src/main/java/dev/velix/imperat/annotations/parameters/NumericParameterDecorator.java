package dev.velix.imperat.annotations.parameters;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.InputParameter;
import dev.velix.imperat.command.parameters.NumericParameter;
import dev.velix.imperat.command.parameters.NumericRange;
import org.jetbrains.annotations.Nullable;

public final class NumericParameterDecorator extends InputParameter implements NumericParameter {
	
	private final CommandParameter parameter;
	private final NumericRange range;
	
	NumericParameterDecorator(CommandParameter parameter, NumericRange range) {
		super(parameter.getName(), parameter.getType(),
						parameter.isOptional(), parameter.isFlag(),
						parameter.isFlag(), parameter.getDefaultValueSupplier());
		this.parameter = parameter;
		this.range = range;
	}
	
	public static NumericParameterDecorator decorate(CommandParameter parameter, NumericRange range) {
		return new NumericParameterDecorator(parameter, range);
	}
	
	/*private void handleIfNumeric() {
		if(!TypeUtility.isNumericType(type) || !element.isAnnotationPresent(Range.class)) {
			range = null;
		}else {
			Range annotation = element.getAnnotation(Range.class);
			range = new NumericRange(annotation.min(), annotation.max());
		}
	}*/
	
	/**
	 * Formats the usage parameter
	 * using the command
	 *
	 * @param command The command owning this parameter
	 * @return the formatted parameter
	 */
	@Override
	public <C> String format(Command<C> command) {
		return parameter.format(command);
	}
	
	/**
	 * @return The actual range of the numeric parameter
	 * returns null if no range is specified !
	 */
	@Override
	public @Nullable NumericRange getRange() {
		return range;
	}
	
}
