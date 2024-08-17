package dev.velix.imperat.context.internal;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.context.CommandFlagExtractor;
import dev.velix.imperat.util.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@ApiStatus.Internal
final class CommandFlagExtractorImpl<C> implements CommandFlagExtractor<C> {
	
	
	private final FlagRegistry flagRegistry;
	
	CommandFlagExtractorImpl() {
		this.flagRegistry = new FlagRegistry();
	}
	
	public static <C> CommandFlagExtractorImpl<C> createNative() {
		return new CommandFlagExtractorImpl<>();
	}
	
	/**
	 * @param rawArgument the raw argument
	 * @return whether the raw argument is considered a flag
	 */
	@Override
	public boolean isArgumentFlag(String rawArgument) {
		return rawArgument != null && rawArgument.startsWith("-");
	}
	
	/**
	 * @param command    the command's data
	 * @param rawArgFlag the raw flag used in the command execution
	 * @return whether this flag is registered and known to be usable for this command
	 */
	@Override
	public boolean isKnownFlag(Command<C> command, String rawArgFlag) {
		if (!isArgumentFlag(rawArgFlag)) return false;
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
	public void extract(@NotNull Command<C> command,
	                    @NotNull ArgumentQueue queue) {
		try {
			
			queue.stream()
							.filter((flagArg) -> isKnownFlag(command, flagArg))
							.map((flagArg) -> {
								String flagAliasUsed = getFlagAliasUsed(flagArg);
								return command.getKnownFlags()
												.searchFlagAlias(flagAliasUsed).orElse(null);
							})
							.filter(Objects::nonNull)
							.forEach((flag) -> flagRegistry.setData(flag.name(), flag));
			
		} catch (Exception exc) {
			throw new RuntimeException(exc);
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
