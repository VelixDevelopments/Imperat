package dev.velix.imperat.context;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the processes context of a command
 * entered by {@link Source}
 *
 * @param <C> the command sender type
 */
@ApiStatus.AvailableSince("1.0.0")
public interface Context<C> extends ExecutionContext<C> {

    /**
     * @return the command source of the command
     * @see Source
     */
    @NotNull
    Source<C> getSource();

    /**
     * @return the number of flags extracted
     */
    int flagsUsedCount();

}
