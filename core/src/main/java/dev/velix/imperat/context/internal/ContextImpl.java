package dev.velix.imperat.context.internal;

import dev.velix.imperat.CommandSource;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.CommandSwitch;
import dev.velix.imperat.context.Context;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
final class ContextImpl<C> implements Context<C> {
	
	private final CommandSource<C> commandSource;
	private final String command;
	private final ArgumentQueue args;
	
	ContextImpl(
					CommandSource<C> commandSource,
					String command,
					ArgumentQueue args) {
		this.commandSource = commandSource;
		this.command = command;
		this.args = args;
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
	public ResolvedFlag getFlag(String flagName) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Fetches the flag input value
	 * returns null if the flag is a {@link CommandSwitch}
	 * OR if the value hasn't been resolved somehow
	 *
	 * @param flagName the flag name
	 * @return the resolved value of the flag input
	 */
	@Override
	public <T> @Nullable T getFlagValue(String flagName) {
		throw new UnsupportedOperationException();
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
	
	
	/**
	 * @return the number of flags extracted
	 */
	@Override
	public int flagsUsedCount() {
		throw new UnsupportedOperationException();
	}
	
}
