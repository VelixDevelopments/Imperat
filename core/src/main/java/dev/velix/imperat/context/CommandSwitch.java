package dev.velix.imperat.context;

/**
 * Represents a flag that returns a value of type boolean
 * which represents merely whether its present in the syntax input or not
 * so they have no input value unlike true flags
 * @see CommandFlag
 */
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
