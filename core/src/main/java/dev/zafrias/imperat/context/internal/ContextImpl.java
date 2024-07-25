package dev.zafrias.imperat.context.internal;

import dev.zafrias.imperat.CommandSource;
import dev.zafrias.imperat.context.ArgumentQueue;
import dev.zafrias.imperat.context.CommandFlagExtractor;
import dev.zafrias.imperat.context.Context;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@ApiStatus.Internal
public final class ContextImpl<C> implements Context<C> {

	private final CommandSource<C> commandSource;
	private final String command;
	private final ArgumentQueue args;

	private final CommandFlagExtractor<C> flagExtractor;

	ContextImpl(CommandSource<C> commandSource,
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
	 * The class responsible for extracting/reading flags
	 * that has been used in the command context {@link CommandFlagExtractor}
	 *
	 * @return the command flag extractor instance
	 */
	@Override
	public @NotNull CommandFlagExtractor<C> getFlagExtractor() {
		return flagExtractor;
	}

}
