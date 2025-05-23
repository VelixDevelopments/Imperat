package dev.velix.imperat.context;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.internal.Argument;
import dev.velix.imperat.exception.ImperatException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * This includes all non-literal arguments (which take the form of literal arguments)
 * this fetches all parameters that got resolved into a value and caches it for
 * further use during command executions.
 * <p>
 * After resolving the arguments, they are cached and then processed during
 * execution time
 * </p>
 *
 * @see Command
 */
@ApiStatus.AvailableSince("1.0.0")
public interface ResolvedContext<S extends Source> extends ExecutionContext<S> {


    /**
     * Resolves the arguments from the given plain input {@link Context}
     */
    void resolve() throws ImperatException;

    /**
     * Fetches the arguments of a command/subcommand that got resolved
     * except for the arguments that represent the literal/subcommand name arguments
     *
     * @param command the command/subcommand owning the argument
     * @param name    the name of the argument
     * @return the argument resolved from raw into a value
     */
    @Nullable
    Argument<S> getResolvedArgument(Command<S> command, String name);

    /**
     * @param command the command/subcommand with certain args
     * @return the command/subcommand's resolved args
     */
    List<Argument<S>> getResolvedArguments(Command<S> command);

    /**
     * @return all {@link Command} that have been used in this context
     */
    @NotNull
    Iterable<? extends Command<S>> getCommandsUsed();

    /**
     * @return an ordered collection of {@link Argument} just like how they were entered
     * NOTE: the flags are NOT included as a resolved argument, it's treated differently
     */
    Collection<? extends Argument<S>> getResolvedArguments();


    void debug();
}
