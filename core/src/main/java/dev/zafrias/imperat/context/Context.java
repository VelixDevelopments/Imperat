package dev.zafrias.imperat.context;

import dev.zafrias.imperat.CommandSource;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the processes context of a command
 * entered by {@link CommandSource}
 *
 * @param <C> the command sender type
 */
public interface Context<C> {

	/**
	 * the command used in the context
	 *
	 * @return the command used
	 */
	@NotNull
	String getCommandUsed();


	/**
	 * @return the command source of the command
	 * @see CommandSource
	 */
	@NotNull
	CommandSource<C> getCommandSource();


	/**
	 * @return the arguments entered by the
	 * @see ArgumentQueue
	 */
	@NotNull
	ArgumentQueue getArguments();


	/**
	 * The class responsible for extracting/reading flags
	 * that has been used in the command context {@link CommandFlagExtractor}
	 *
	 * @return the command flag extractor instance
	 */
	@NotNull
	CommandFlagExtractor<C> getFlagExtractor();

}
