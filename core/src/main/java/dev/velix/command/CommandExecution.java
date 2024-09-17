package dev.velix.command;

import dev.velix.context.ExecutionContext;
import dev.velix.context.Source;
import dev.velix.exception.ImperatException;
import org.jetbrains.annotations.ApiStatus;

/**
 * This class represents the execution/action of this command that's triggered when
 * the sender asks for this command to be executed.
 *
 * @param <S> the command sender type
 */
@ApiStatus.AvailableSince("1.0.0")
public interface CommandExecution<S extends Source> {
    
    /**
     * Executes the command's actions
     *
     * @param source  the source/sender of this command
     * @param context the context of the command
     */
    void execute(final S source, final ExecutionContext<S> context) throws ImperatException;
    
}
