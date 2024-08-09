package dev.velix.imperat.context;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.CommandSource;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.internal.CommandFlagExtractorImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Represents a way for defining
 * how the context is created
 *
 * @param <C>
 */
@ApiStatus.AvailableSince("1.0.0")
public interface ContextFactory<C> {

	default @NotNull Context<C> createContext(
			  @NotNull CommandDispatcher<C> dispatcher,
			  @NotNull CommandSource<C> commandSource,
			  @NotNull String command,
			  @NotNull ArgumentQueue queue
	) {
		return createContext(dispatcher, commandSource,
				  command, queue, CommandFlagExtractorImpl::createNative);
	}

	/**
	 * @param dispatcher            the dispatcher
	 * @param commandSource         the sender/source of this command execution
	 * @param command               the command label used
	 * @param queue                 the args input
	 * @param flagExtractorSupplier supplies the flag extractor
	 * @return new context from the command and args used by {@link CommandSource}
	 */
	@NotNull
	Context<C> createContext(
			  @NotNull CommandDispatcher<C> dispatcher,
			  @NotNull CommandSource<C> commandSource,
			  @NotNull String command,
			  @NotNull ArgumentQueue queue,
			  @NotNull Supplier<CommandFlagExtractor<C>> flagExtractorSupplier
	);

	/**
	 * @param command      the command that's running
	 * @param plainContext the context plain
	 * @return the context after resolving args into values for
	 * later on parsing it into the execution
	 */
	ResolvedContext<C> createResolvedContext(@NotNull CommandDispatcher<C> dispatcher,
	                                         @NotNull Command<C> command,
	                                         @NotNull Context<C> plainContext
	);

}
