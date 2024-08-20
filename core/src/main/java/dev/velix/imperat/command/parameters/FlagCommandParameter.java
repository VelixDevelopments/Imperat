package dev.velix.imperat.command.parameters;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.context.CommandSwitch;
import dev.velix.imperat.resolvers.OptionalValueSupplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ApiStatus.Internal
public final class FlagCommandParameter extends InputParameter implements FlagParameter {
	
	private final CommandFlag flag;
	private final OptionalValueSupplier<?> supplier;
	
	FlagCommandParameter(String flagName, List<String> aliases, Class<?> inputType, OptionalValueSupplier<?> supplier) {
		super(flagName, CommandFlag.class,
						true, true, false, null);
		flag = CommandFlag.create(flagName, aliases, inputType);
		this.supplier = supplier;
	}
	
	FlagCommandParameter(CommandSwitch commandSwitch, OptionalValueSupplier<?> supplier) {
		super(commandSwitch.name(), CommandSwitch.class, true, true, false, null);
		this.flag = commandSwitch;
		this.supplier = supplier;
	}
	
	@Override
	public <C> String format(Command<C> command) {
		return flag.format();
	}
	
	/**
	 * @return The flag's data
	 */
	@Override
	public @NotNull CommandFlag getFlag() {
		return flag;
	}
	
	/**
	 * @return the default value if it's input is not present
	 * in case of the parameter being optional
	 */
	@Override @SuppressWarnings("unchecked")
	public <T> OptionalValueSupplier<T> getDefaultValueSupplier() {
		return (OptionalValueSupplier<T>) supplier;
	}
}
