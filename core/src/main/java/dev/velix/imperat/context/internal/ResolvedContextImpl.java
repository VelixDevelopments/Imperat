package dev.velix.imperat.context.internal;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.CommandSource;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.UsageParameter;
import dev.velix.imperat.context.CommandFlagExtractor;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.exceptions.CommandException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

/**
 * The purpose of this class is to resolve string inputs
 * into values , each subcommand {@link Command} may have its own args
 * Therefore, it loads the arguments per subcommand USED in the context input
 * from the command source/sender
 * the results (resolved arguments) will be used later on to
 * find the command usage object then use the found command usage to execute the action.
 *
 * @param <C> the sender type
 */
@ApiStatus.Internal
public final class ResolvedContextImpl<C> implements ResolvedContext<C> {

	private final Command<C> commandUsed;
	private final Context<C> context;

	//per command/subcommand because the class 'Command' is can be also treated as a sub command
	private final Map<Command<C>, Map<String, ResolvedArgument>> resolvedArgumentsPerCommand = new LinkedHashMap<>();

	//all resolved arguments EXCEPT for subcommands and flags.
	private final Map<String, ResolvedArgument> allResolvedArgs = new LinkedHashMap<>();


	ResolvedContextImpl(Command<C> commandUsed,
	                    Context<C> context) {
		this.commandUsed = commandUsed;
		this.context = context;
	}

	/**
	 * @return The owning parent-command for all of these arguments
	 */
	@Override
	public Command<C> getOwningCommand() {
		return commandUsed;
	}

	/**
	 * the command used in the context
	 *
	 * @return the command used
	 */
	@Override
	public @NotNull String getCommandUsed() {
		return context.getCommandUsed();
	}

	/**
	 * Fetches the arguments of a command/subcommand that got resolved
	 * except for the arguments that represent the literal/subcommand name arguments
	 *
	 * @param command the command/subcommand owning the argument
	 * @param name    the name of the argument
	 * @return the argument resolved from raw into a value
	 */
	@Override
	public @Nullable ResolvedArgument getResolvedArgument(Command<C> command, String name) {
		Map<String, ResolvedArgument> resolvedArgs = resolvedArgumentsPerCommand.get(command);
		if(resolvedArgs == null) return null;

		return resolvedArgs.get(name);
	}

	/**
	 * @param command the command/subcommand with certain args
	 * @return the command/subcommand's resolved args in as new array-list
	 */
	@Override
	public List<ResolvedArgument> getResolvedArguments(Command<C> command) {
		Map<String, ResolvedArgument> argMap = resolvedArgumentsPerCommand.get(command);
		if(argMap == null) return Collections.emptyList();
		return new ArrayList<>(argMap.values());
	}

	/**
	 * @return all {@link Command} that have been used in this context
	 */
	@Override
	public @NotNull Collection<? extends Command<C>> getCommandsUsed() {
		return resolvedArgumentsPerCommand.keySet();
	}

	/**
	 * @return an ordered collection of {@link ResolvedArgument} just like how they were entered
	 * NOTE: the flags are NOT included as a resolved argument, it's treated in a different way
	 */
	@Override
	public Collection<? extends ResolvedArgument> getResolvedArguments() {
		return allResolvedArgs.values();
	}

	/**
	 * Fetches a resolved argument's value
	 *
	 * @param name the name of the command
	 * @return the value of the resolved argument
	 * @see ResolvedArgument
	 */
	@Override @SuppressWarnings("unchecked")
	public <T> @Nullable T getArgument(String name) {
		ResolvedArgument argument = allResolvedArgs.get(name.toLowerCase());
		if(argument == null)return null;
		return (T) argument.getValue();
	}


	/**
	 * @return the command source of the command
	 * @see CommandSource
	 */
	@Override
	public @NotNull CommandSource<C> getCommandSource() {
		return context.getCommandSource();
	}

	/**
	 * @return the arguments entered by the
	 * @see ArgumentQueue
	 */
	@Override
	public @NotNull ArgumentQueue getArguments() {
		return context.getArguments();
	}

	/**
	 * @param flagName the name of the flag to check if it's used or not
	 * @return The flag whether it has been used or not in this command context
	 */
	@Override
	public boolean getFlag(String flagName) {
		return getFlagExtractor().getExtractedFlags()
				  .getData(flagName).isPresent();
	}

	/**
	 * The class responsible for extracting/reading flags
	 * that has been used in the command context {@link CommandFlagExtractor}
	 *
	 * @return the command flag extractor instance
	 */
	@Override
	public @NotNull CommandFlagExtractor<C> getFlagExtractor() {
		return context.getFlagExtractor();
	}

	/**
	 * @return the number of flags extracted
	 * by {@link CommandFlagExtractor}
	 */
	@Override
	public int flagsUsedCount() {
		return context.flagsUsedCount();
	}

	/**
	 * Resolves the arguments from the given plain input {@link Context}
	 * @param dispatcher the dispatcher handling all commands
	 */
	@Override
	public void resolve(CommandDispatcher<C> dispatcher, CommandUsage<C> usage) throws CommandException {
		SmartUsageResolve<C> handler = SmartUsageResolve.create(commandUsed, usage);
		handler.resolve(dispatcher, this);
	}


	@Override
	public <T> void resolveArgument(Command<C> command,
	                                @Nullable String raw,
	                                int index,
	                                UsageParameter parameter,
	                                @Nullable T value) {

		final ResolvedArgument argument = new ResolvedArgument(raw, parameter, index, value);
		resolvedArgumentsPerCommand.compute(command, (existingCmd, existingResolvedArgs)-> {
			if(existingResolvedArgs != null) {
				existingResolvedArgs.put(parameter.getName(), argument);
				return existingResolvedArgs;
			}
			Map<String, ResolvedArgument> args = new LinkedHashMap<>();
			args.put(parameter.getName(), argument);
			return args;
		});
		allResolvedArgs.put(parameter.getName(), argument);
	}

}
