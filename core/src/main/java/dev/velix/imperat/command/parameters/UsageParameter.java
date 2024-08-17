package dev.velix.imperat.command.parameters;

import dev.velix.imperat.annotations.parameters.AnnotatedParameter;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.resolvers.OptionalValueSupplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the command parameter required
 * by the usage of the command itself
 */
@ApiStatus.AvailableSince("1.0.0")
public interface UsageParameter {
	
	/**
	 * @return the name of the parameter
	 */
	String getName();
	
	/**
	 * @return the index of this parameter
	 */
	int getPosition();
	
	/**
	 * Sets the position of this parameter in a syntax
	 * DO NOT USE THIS FOR ANY REASON unless it's necessary to do so
	 *
	 * @param position the position to set
	 */
	@ApiStatus.Internal
	void setPosition(int position);
	
	/**
	 * @return the value type of this parameter
	 */
	Class<?> getType();
	
	/**
	 * @return the default value if it's input is not present
	 * in case of the parameter being optional
	 */
	<C> OptionalValueSupplier<C, ?> getDefaultValueSupplier();
	
	/**
	 * @return whether this is an optional argument
	 */
	boolean isOptional();
	
	
	/**
	 * @return checks whether this parameter is a flag
	 */
	boolean isFlag();
	
	
	/**
	 * @return checks whether this parameter
	 * consumes all the args input after it.
	 */
	boolean isGreedy();
	
	/**
	 * @return checks whether this usage param is a command name
	 */
	default boolean isCommand() {
		return this instanceof Command;
	}
	
	/**
	 * Casts the parameter to a subcommand/command
	 *
	 * @return the parameter as a command
	 */
	Command<?> asCommand();
	
	/**
	 * @return Whether this usage parameter has been constructed
	 * using the annotations through methods or not
	 */
	default boolean isAnnotated() {
		return this instanceof AnnotatedParameter;
	}
	
	/**
	 * Casts the parameter to a parameter with annotations
	 *
	 * @return the parameter as annotated one
	 * @see AnnotatedParameter
	 */
	default AnnotatedParameter asAnnotated() {
		return (AnnotatedParameter) this;
	}
	
	/**
	 * Formats the usage parameter
	 * using the command
	 *
	 * @param command The command owning this parameter
	 * @return the formatted parameter
	 */
	<C> String format(Command<C> command);
	
	
	static <T> UsageParameter required(String name, Class<T> clazz) {
		return new NormalUsageParameter(name, clazz, false, false, null);
	}
	
	static <C, T> UsageParameter optional(String name, Class<T> clazz, @Nullable OptionalValueSupplier<C, T> defaultValue) {
		return new NormalUsageParameter(name, clazz, true, false, defaultValue);
	}
	
	static <C> UsageParameter greedy(String name, boolean optional, @Nullable OptionalValueSupplier<C, String> defaultValue) {
		return new NormalUsageParameter(name, String.class, optional, true, defaultValue);
	}
	
	static UsageParameter requiredText(String name) {
		return required(name, String.class);
	}
	
	static UsageParameter requiredInt(String name) {
		return required(name, Integer.class);
	}
	
	static UsageParameter requiredLong(String name) {
		return required(name, Long.class);
	}
	
	static UsageParameter requiredDouble(String name) {
		return required(name, Double.class);
	}
	
	static UsageParameter flag(String flagName) {
		return new FlagUsageParameter(flagName);
	}
	
	
}
