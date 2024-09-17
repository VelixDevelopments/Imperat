package dev.velix.context;

import dev.velix.command.Command;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the processes context of a command
 * entered by {@link Source}
 *
 * @param <S> the command sender type
 */
@ApiStatus.AvailableSince("1.0.0")
public interface Context<S extends Source> {
    
    /**
     * @return The {@link Command} owning this context.
     */
    Command<S> getCommandUsed();
    
    /**
     * @return the command source of the command
     * @see Source
     */
    @NotNull
    S getSource();
    
    
    /**
     * @return the arguments entered by the
     * @see ArgumentQueue
     */
    ArgumentQueue getArguments();
    
    
}
