package dev.velix.command.parameters;

import dev.velix.context.CommandFlag;
import dev.velix.context.CommandSwitch;
import org.jetbrains.annotations.NotNull;

public interface FlagParameter extends CommandParameter {
    
    /**
     * @return The flag's data
     */
    @NotNull CommandFlag getFlagData();
    
    /**
     * @return checks whether this parameter is a flag
     */
    @Override
    default boolean isFlag() {
        return true;
    }
    
    default boolean isSwitch() {
        return getFlagData() instanceof CommandSwitch;
    }
}
