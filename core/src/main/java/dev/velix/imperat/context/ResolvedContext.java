package dev.velix.imperat.context;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.UsageParameter;
import dev.velix.imperat.context.internal.ResolvedArgument;
import dev.velix.imperat.exceptions.CommandException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * This includes all non-literal arguments(which take the  form of literal arguments)
 * this fetches all parameters that got resolved into a value and caches it for
 * further use during command executions.
 * <p>
 * After resolving the arguments, they are cached and then processed during
 * execution time
 * </p>
 *
 * @see Command
 */
public interface ResolvedContext<C> extends Context<C> {

	/**
	 * @return The owning parent-command for all of these arguments
	 */
	Command<C> getOwningCommand();

	/**
	 * Resolves the arguments from the given plain input {@link Context}
	 *
	 * @param dispatcher the dispatcher handling all commands
	 * @param usage      the usage that were determined suitable for resolving the args
	 */
	void resolve(CommandDispatcher<C> dispatcher, CommandUsage<C> usage) throws CommandException;

	/**
	 * Fetches the arguments of a command/subcommand that got resolved
	 * except for the arguments that represent the literal/subcommand name arguments
	 *
	 * @param command the command/subcommand owning the argument
	 * @param name    the name of the argument
	 * @return the argument resolved from raw into a value
	 */
	@Nullable
	ResolvedArgument getResolvedArgument(Command<C> command, String name);

	/**
	 * @param command the command/subcommand with certain args
	 * @return the command/subcommand's resolved args
	 */
	List<ResolvedArgument> getResolvedArguments(Command<C> command);

	/**
	 * @return all {@link Command} that have been used in this context
	 */
	@NotNull
	Collection<? extends Command<C>> getCommandsUsed();

	/**
	 * @return an ordered collection of {@link ResolvedArgument} just like how they were entered
	 * NOTE: the flags are NOT included as a resolved argument, it's treated in a different way
	 */
	Collection<? extends ResolvedArgument> getResolvedArguments();

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
	<T> void resolveArgument(Command<C> command,
	                         @Nullable String raw,
	                         int index,
	                         UsageParameter parameter,
	                         @Nullable T value);

}
