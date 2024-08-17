package dev.velix.imperat.context;

import dev.velix.imperat.CommandSource;
import dev.velix.imperat.command.Command;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the processes context of a command
 * entered by {@link CommandSource}
 *
 * @param <C> the command sender type
 */
@ApiStatus.AvailableSince("1.0.0")
public interface Context<C> extends ExecutionContext {
	
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
	 * The class responsible for extracting/reading flags
	 * that has been used in the command context {@link CommandFlagExtractor}
	 *
	 * @return the command flag extractor instance
	 */
	@NotNull
	CommandFlagExtractor<C> getFlagExtractor();
	
	default void extractCommandFlags(Command<C> command) {
		getFlagExtractor().extract(command, this);
	}
	
	/**
	 * @return the number of flags extracted
	 * by {@link CommandFlagExtractor}
	 */
	int flagsUsedCount();
}
