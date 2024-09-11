package dev.velix.imperat.context;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the processes context of a command
 * entered by {@link Source}
 *
 * @param <S> the command sender type
 */
@ApiStatus.AvailableSince("1.0.0")
public interface Context<S extends Source> extends ExecutionContext<S> {
    
    /**
     * @return the command source of the command
     * @see Source
     */
    @NotNull
    S getSource();
    
    /**
     * @return the number of flags extracted
     */
    int flagsUsedCount();
    
}
