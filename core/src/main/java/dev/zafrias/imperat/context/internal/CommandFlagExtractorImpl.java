package dev.zafrias.imperat.context.flags.internal;

import dev.zafrias.imperat.Command;
import dev.zafrias.imperat.Result;
import dev.zafrias.imperat.context.ArgumentQueue;
import dev.zafrias.imperat.context.Context;
import dev.zafrias.imperat.context.flags.CommandFlag;
import dev.zafrias.imperat.context.flags.CommandFlagExtractor;
import dev.zafrias.imperat.util.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@ApiStatus.Internal
public final class CommandFlagExtractorImpl<C> implements CommandFlagExtractor<C> {

	private final Context<C> context;
	private final Registry<String, CommandFlag> flagRegistry;

	CommandFlagExtractorImpl(Context<C> context) {
		this.context = context;
		this.flagRegistry = new Registry<>();
	}

	/**
	 * @return the context by which
	 * the flag extractor will be using to
	 * extract the used flags in this execution
	 */
	@Override
	public @NotNull Context<C> getContext() {
		return context;
	}

	/**
	 * @param rawArgument the raw argument
	 * @return whether the raw argument is considered a flag
	 */
	@Override
	public boolean isArgumentFlag(String rawArgument) {
		return rawArgument.startsWith("-");
	}

	/**
	 * @param command    the command's data
	 * @param rawArgFlag the raw flag used in the command execution
	 * @return whether this flag is registered and known to be usable for this command
	 */
	@Override
	public boolean isKnownFlag(Command<C> command, String rawArgFlag) {
		assert isArgumentFlag(rawArgFlag);
		String flagAliasUsed = getFlagAliasUsed(rawArgFlag);
		return command.getKnownFlags()
				  .search((name, flag) -> flag.hasAlias(flagAliasUsed))
				  .isPresent();
	}


	/**
	 * Extracts the flags used in this argument queue
	 * <p>
	 * it caches the results ONLY if the result isn't failure
	 * so assuming it succeeds and no error happens during the extraction process,
	 * it will cache the flags extracted into the flag registry before returning that registry
	 * </p>
	 *
	 * @param queue the queue to use for extracting the flags
	 */
	@Override
	public Result<FlagRegistry> extract(Command<C> command, ArgumentQueue queue) {
		try {
			return Result.success(
					  queue.stream()
					 .filter(this::isArgumentFlag)
					 .filter((flagArg) -> isKnownFlag(command, flagArg))
					 .map((flagArg) -> {
						 String flagAliasUsed = getFlagAliasUsed(flagArg);
						 return command.getKnownFlags()
									.searchFlagAlias(flagAliasUsed).orElse(null);
					 })
					 .filter(Objects::nonNull)
					 .collect(FlagRegistry.COLLECTOR)
			);
		}catch (Exception exc) {
			return Result.fail(exc);
		}
	}

	/**
	 * @return the flags that has been extract
	 * by the method {@link CommandFlagExtractor#extract(Command, ArgumentQueue)}
	 */
	@Override
	public Registry<String, CommandFlag> getExtractedFlags() {
		return flagRegistry;
	}


	private String getFlagAliasUsed(String rawFlagArg) {
		return rawFlagArg.substring(1);
	}
}
