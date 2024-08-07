package dev.velix.imperat.command;

import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

@ApiStatus.Internal
abstract class InputParameter implements UsageParameter {

	private final String name;
	private int index;
	private final Class<?> type;
	private final boolean literal, optional, flag, greedy;
	private final Object defaultValue;

	protected InputParameter(String name, Class<?> type,
	                         boolean literal, boolean optional, boolean flag, boolean greedy,
	                         Object defaultValue) {
		this.name = name;
		this.type = type;
		this.literal = literal;
		this.optional = optional;
		this.flag = flag;
		this.greedy = greedy;
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the name of the parameter
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the index of this parameter
	 */
	@Override
	public int getPosition() {
		return index;
	}

	/**
	 * Sets the position of this parameter in a syntax
	 * DO NOT USE THIS FOR ANY REASON unless it's necessary to do so
	 *
	 * @param position the position to set
	 */
	@Override
	public void setPosition(int position) {
		this.index = position;
	}

	/**
	 * @return the value type of this parameter
	 */
	@Override
	public Class<?> getType() {
		return type;
	}

	/**
	 * @return the default value if it's input is not present
	 * in case of the parameter being optional
	 */
	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return is a literal string parameter
	 * which can be treated as a sub command
	 * WARNING: NOT RECOMMENDED TO USE THIS , instead use
	 * the well documented subcommands API.
	 */
	@Override
	public boolean isLiteral() {
		return literal;
	}

	/**
	 * @return whether this is an optional argument
	 */
	@Override
	public boolean isOptional() {
		return optional;
	}

	/**
	 * @return checks whether this parameter is a flag
	 */
	@Override
	public boolean isFlag() {
		return flag;
	}

	/**
	 * @return checks whether this parameter
	 * consumes all the args input after it.
	 */
	@Override
	public boolean isGreedy() {
		if (this.type != String.class && greedy) {
			throw new IllegalStateException(
					  String.format("Usage parameter '%s' cannot be greedy while having value-type '%s'", name, type.getName())
			);
		}
		return greedy;
	}

	@Override
	public Command<?> asCommand() {
		throw new UnsupportedOperationException("Non-Command Parameter cannot be converted into a command parameter");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InputParameter that = (InputParameter) o;
		return Objects.equals(name, that.name)
				  && Objects.equals(type, that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}

	@Override
	public String toString() {
		return format(null);
	}
}