package dev.velix.imperat.command;

import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ImperatException;
import org.jetbrains.annotations.ApiStatus;

/**
 * This class represents the execution/action of this command that's triggered when
 * the sender asks for this command to be executed.
 *
 * @param <S> the command sender type
 */
@ApiStatus.AvailableSince("1.0.0")
public interface CommandExecution<S extends Source> {

    static <S extends Source> CommandExecution<S> empty() {
        return (source, context) -> {
        };
    }

    /**
     * Executes the command's actions
     *
     * @param source  the source/sender of this command
     * @param context the context of the command
     */
    void execute(final S source, final ExecutionContext<S> context) throws ImperatException;

}
