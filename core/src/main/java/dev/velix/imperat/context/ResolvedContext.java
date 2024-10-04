package dev.velix.imperat.context;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.internal.ResolvedArgument;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.resolvers.ValueResolver;
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
    ResolvedArgument<S> getResolvedArgument(Command<S> command, String name);

    /**
     * @param command the command/subcommand with certain args
     * @return the command/subcommand's resolved args
     */
    List<ResolvedArgument<S>> getResolvedArguments(Command<S> command);

    /**
     * @return all {@link Command} that have been used in this context
     */
    @NotNull
    Iterable<? extends Command<S>> getCommandsUsed();

    /**
     * @return an ordered collection of {@link ResolvedArgument} just like how they were entered
     * NOTE: the flags are NOT included as a resolved argument, it's treated differently
     */
    Collection<? extends ResolvedArgument<S>> getResolvedArguments();

    /**
     * Resolves the raw input and
     * the parameters into arguments {@link ResolvedArgument}
     *
     * @param command   the command owning the argument
     * @param raw       the raw input
     * @param index     the position of this argument
     * @param parameter the parameter of the argument
     * @param value     the resolved value of the argument
     * @param <T>       the type of resolved value of the argument
     */
    <T> void resolveArgument(
        Command<S> command,
        @Nullable String raw,
        int index,
        CommandParameter<S> parameter,
        @Nullable T value
    ) throws ImperatException;

    /**
     * Resolves flag the in the context
     *
     * @param flagRaw        the flag itself raw input
     * @param flagInputRaw   the flag's value if present
     * @param flagInputValue the flag's input value resolved by {@link ValueResolver}
     * @param flagDetected   the optional flag-parameter detected
     */
    void resolveFlag(
        String flagRaw,
        @Nullable String flagInputRaw,
        @Nullable Object flagInputValue,
        CommandFlag flagDetected
    );

    /**
     * Fetches the last used resolved command
     * of a resolved context!
     *
     * @return the last used command/subcommand
     */
    Command<S> getLastUsedCommand();

    /**
     * @return The used usage to use it to resolve commands
     */
    CommandUsage<S> getDetectedUsage();
}
