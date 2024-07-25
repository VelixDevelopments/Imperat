package dev.zafrias.imperat.context.internal;

import dev.zafrias.imperat.CommandSource;
import dev.zafrias.imperat.context.ArgumentQueue;
import dev.zafrias.imperat.context.CommandFlagExtractor;
import dev.zafrias.imperat.context.Context;
import dev.zafrias.imperat.context.ContextFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@ApiStatus.Internal
public final class DefaultContextFactory<C> implements ContextFactory<C> {


	/**
	 * @param commandSource         the sender/source of this command execution
	 * @param command               the command label used
	 * @param queue                 the args input
	 * @param flagExtractorSupplier supplies the flag extractor
	 * @return new context from the command and args used by {@link CommandSource}
	 */
	@Override
	public @NotNull Context<C> createContext(@NotNull CommandSource<C> commandSource,
	                                         @NotNull String command,
	                                         @NotNull ArgumentQueue queue,
	                                         @NotNull Supplier<CommandFlagExtractor<C>> flagExtractorSupplier) {
		return new ContextImpl<>(commandSource, command, queue, CommandFlagExtractorImpl::new);
	}
}
