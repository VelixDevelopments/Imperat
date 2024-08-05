package dev.velix.imperat.command;

@Deprecated
public final class LiteralUsageParameter extends InputParameter {


	LiteralUsageParameter(String name) {
		super(name, String.class, true, false, false, false, name);
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
		return getName();
	}
}
