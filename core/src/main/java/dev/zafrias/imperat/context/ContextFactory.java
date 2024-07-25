package dev.zafrias.imperat.context;

import dev.zafrias.imperat.CommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Represents a way for defining
 * how the context is created
 *
 * @param <C>
 */
public interface ContextFactory<C> {

	/**
	 * @param commandSource         the sender/source of this command execution
	 * @param command               the command label used
	 * @param queue                 the args input
	 * @param flagExtractorSupplier supplies the flag extractor
	 * @return new context from the command and args used by {@link CommandSource}
	 */
	@NotNull
	Context<C> createContext(
			  @NotNull CommandSource<C> commandSource,
			  @NotNull String command,
			  @NotNull ArgumentQueue queue,
			  @NotNull Supplier<CommandFlagExtractor<C>> flagExtractorSupplier
	);

}
