package dev.velix.imperat.command;

import dev.velix.imperat.Source;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.exceptions.CommandException;
import org.jetbrains.annotations.ApiStatus;

/**
 * This class represents the execution/action of this command that's triggered when
 * the sender asks for this command to be executed.
 *
 * @param <C> the command sender type
 */
@ApiStatus.AvailableSince("1.0.0")
public interface CommandExecution<C> {

    /**
     * Executes the command's actions
     *
     * @param source  the source/sender of this command
     * @param context the context of the command
     */
    void execute(Source<C> source,
                 ExecutionContext context) throws CommandException;

}
