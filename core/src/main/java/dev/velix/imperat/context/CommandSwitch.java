package dev.velix.imperat.context;

public interface CommandSwitch extends CommandFlag {
	
	/**
	 * @return the type of input
	 * from the flag
	 */
	@Override
	default Class<?> inputType() {
		throw new UnsupportedOperationException("Command Switches are declared " +
						"by their presence merely no input types");
	}
	
}
