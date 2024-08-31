package dev.velix.imperat.context;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a flag that returns a value of type boolean
 * which represents merely whether its present in the syntax input or
 * so they have no input value unlike true flags
 *
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
    
    static CommandSwitch create(String name, List<String> aliases) {
        return new CommandSwitch.CommandSwitchImpl(CommandFlag.create(name, aliases, null));
    }
    
    record CommandSwitchImpl(CommandFlag flag) implements CommandSwitch {
        
        /**
         * The main name of the flag
         *
         * @return the name(unique) of the flag
         */
        @Override
        public @NotNull String name() {
            return flag.name();
        }
        
        /**
         * @return the alias of the flag
         */
        @Override
        public @NotNull List<String> aliases() {
            return flag.aliases();
        }
        
    }
}
