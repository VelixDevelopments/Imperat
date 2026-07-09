package studio.mevera.imperat.command;

import org.jetbrains.annotations.ApiStatus;
import studio.mevera.imperat.context.CommandSource;
import studio.mevera.imperat.context.ExecutionContext;
import studio.mevera.imperat.exception.CommandException;

/**
 * This class represents the execution/action of this command that's triggered when
 * the sender asks for this command to be executed.
 *
 * @param <S> the command sender valueType
 */
@ApiStatus.AvailableSince("1.0.0")
public interface CommandExecution<S extends CommandSource> {

    /**
     * Shared no-op execution singleton. A single instance (rather than a fresh
     * lambda per call) so callers can detect "no execution was ever supplied"
     * by identity — see {@link CommandPathway#isExecutable()}.
     */
    @ApiStatus.Internal
    CommandExecution<CommandSource> EMPTY = (source, context) -> {
    };

    @SuppressWarnings("unchecked")
    static <S extends CommandSource> CommandExecution<S> empty() {
        // Safe: the no-op body never touches the source, so narrowing the
        // source type parameter cannot misbehave.
        return (CommandExecution<S>) EMPTY;
    }

    /**
     * Executes the command's actions
     *
     * @param source  the source/sender of this command
     * @param context the context of the command
     */
    void execute(final S source, final ExecutionContext<S> context) throws CommandException;

}
