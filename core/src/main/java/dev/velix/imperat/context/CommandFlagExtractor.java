package dev.velix.imperat.context;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.util.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a way of extracting/loading flags
 * from a given context
 *
 * @param <C>
 */
@ApiStatus.AvailableSince("1.0.0")
public interface CommandFlagExtractor<C> {


	/**
	 * @param rawArgument the raw argument
	 * @return whether the raw argument is considered a flag
	 */
	boolean isArgumentFlag(String rawArgument);

	/**
	 * @param command    the command's data
	 * @param rawArgFlag the raw flag used in the command execution
	 * @return whether this flag is registered and known to be usable for this command
	 */
	boolean isKnownFlag(Command<C> command, String rawArgFlag);


	/**
	 * Extracts the flags used in this argument queue
	 * <p>
	 * it caches the results ONLY if the result isn't failure
	 * so assuming it succeeds and no error happens during the extraction process,
	 * it will cache the flags extracted into the flag registry before returning that registry
	 * </p>
	 *
	 * @param command the command's data
	 * @param queue   the queue to use for extracting the flags
	 */
	void extract(@NotNull Command<C> command, @NotNull ArgumentQueue queue);

	/**
	 * Extracts the flags
	 * using the cached context
	 *
	 * @param command the command's data
	 * @param context the context for extraction
	 */
	default void extract(@NotNull Command<C> command, @NotNull Context<C> context) {
		extract(command, context.getArguments());
	}

	/**
	 * @return the flags that has been extract
	 * by the method {@link CommandFlagExtractor#extract(Command, ArgumentQueue)}
	 */
	Registry<String, CommandFlag> getExtractedFlags();

}
