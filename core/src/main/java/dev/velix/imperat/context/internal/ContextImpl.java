package dev.velix.imperat.context.internal;

import dev.velix.imperat.CommandSource;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.CommandFlagExtractor;
import dev.velix.imperat.context.Context;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@ApiStatus.Internal
public final class ContextImpl<C> implements Context<C> {
	
	private final CommandSource<C> commandSource;
	private final String command;
	private final ArgumentQueue args;
	private final CommandFlagExtractor<C> flagExtractor;
	
	ContextImpl(
					CommandSource<C> commandSource,
					String command,
					ArgumentQueue args,
					Supplier<CommandFlagExtractor<C>> flagExtractor) {
		this.commandSource = commandSource;
		this.command = command;
		this.args = args;
		this.flagExtractor = flagExtractor.get();
	}
	
	
	/**
	 * the command used in the context
	 *
	 * @return the command used
	 */
	@Override
	public @NotNull String getCommandUsed() {
		return command;
	}
	
	/**
	 * @return the command source of the command
	 * @see CommandSource
	 */
	@Override
	public @NotNull CommandSource<C> getCommandSource() {
		return commandSource;
	}
	
	/**
	 * @return the arguments entered by the
	 * @see ArgumentQueue
	 */
	@Override
	public @NotNull ArgumentQueue getArguments() {
		return args;
	}
	
	/**
	 * @param flagName the name of the flag to check if it's used or not
	 * @return The flag whether it has been used or not in this command context
	 */
	@Override
	public boolean getFlag(String flagName) {
		return getFlagExtractor().getExtractedFlags().getData(flagName).isPresent();
	}
	
	/**
	 * Fetches a resolved argument's value
	 *
	 * @param name the name of the command
	 * @return the value of the resolved argument
	 * @see ResolvedArgument
	 */
	@Override
	public <T> @Nullable T getArgument(String name) {
		throw new UnsupportedOperationException();
	}
	
	//1- remove default values in usage parameter
	//2- add optional resolvers + registry
	//3- add mappings for defaults in context
	
	
	/**
	 * The class responsible for extracting/reading flags
	 * that has been used in the command context {@link CommandFlagExtractor}
	 *
	 * @return the command flag extractor instance
	 */
	@Override
	public @NotNull CommandFlagExtractor<C> getFlagExtractor() {
		return flagExtractor;
	}
	
	/**
	 * @return the number of flags extracted
	 * by {@link CommandFlagExtractor}
	 */
	@Override
	public int flagsUsedCount() {
		return flagExtractor.getExtractedFlags().getAll().size();
	}
	
}
