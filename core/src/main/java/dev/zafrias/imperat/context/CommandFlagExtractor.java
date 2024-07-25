package dev.zafrias.imperat.context.flags;

import dev.zafrias.imperat.Command;
import dev.zafrias.imperat.Result;
import dev.zafrias.imperat.context.ArgumentQueue;
import dev.zafrias.imperat.context.Context;
import dev.zafrias.imperat.context.flags.internal.FlagRegistry;
import dev.zafrias.imperat.util.Registry;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a way of extracting/loading flags
 * from a given context
 * @param <C>
 */
public interface CommandFlagExtractor<C> {


	/**
	 * @return the context by which
	 * the flag extractor will be using to
	 * extract the used flags in this execution
	 */
	@NotNull
	Context<C> getContext();


	/**
	 * @param rawArgument the raw argument
	 * @return whether the raw argument is considered a flag
	 */
	boolean isArgumentFlag(String rawArgument);

	/**
	 * @param command the command's data
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
	 * @param command the command's data
	 * @param queue the queue to use for extracting the flags
	 */
	Result<FlagRegistry> extract(Command<C> command, ArgumentQueue queue);

	/**
	 * Extracts the flags
	 * using the cached context
	 * @param command the command's data
	 */
	default Result<FlagRegistry> extract(Command<C> command) {
		return extract(command, getContext().getArguments());
	}

	/**
	 * @return the flags that has been extract
	 * by the method {@link CommandFlagExtractor#extract(Command, ArgumentQueue)}
	 */
	Registry<String, CommandFlag> getExtractedFlags();

}
