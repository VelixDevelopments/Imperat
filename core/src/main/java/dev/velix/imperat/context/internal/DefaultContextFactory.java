package dev.velix.imperat.context.internal;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.CommandSource;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@ApiStatus.Internal
public class DefaultContextFactory<C> implements ContextFactory<C> {
	
	
	/**
	 * @param commandSource         the sender/source of this command execution
	 * @param command               the command label used
	 * @param queue                 the args input
	 * @param flagExtractorSupplier supplies the flag extractor
	 * @return new context from the command and args used by {@link CommandSource}
	 */
	@Override
	public @NotNull Context<C> createContext(
					@NotNull CommandDispatcher<C> dispatcher,
					@NotNull CommandSource<C> commandSource,
					@NotNull String command,
					@NotNull ArgumentQueue queue,
					@NotNull Supplier<CommandFlagExtractor<C>> flagExtractorSupplier
	) {
		return new ContextImpl<>(commandSource, command, queue, flagExtractorSupplier);
	}
	
	/**
	 * @param command      the command that's running
	 * @param plainContext the context plain
	 * @return the context after resolving args into values for
	 * later on parsing it into the execution
	 */
	@Override
	public ResolvedContext<C> createResolvedContext(
					@NotNull CommandDispatcher<C> dispatcher,
					@NotNull Command<C> command,
					@NotNull Context<C> plainContext
	) {
		
		return new ResolvedContextImpl<>(
						command,
						plainContext
		);
	}
}
