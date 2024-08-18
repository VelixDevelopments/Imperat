package dev.velix.imperat.help;

import dev.velix.imperat.CommandSource;
import dev.velix.imperat.context.Context;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a help execution instructions
 *
 * @param <C> the type of sender
 */
@FunctionalInterface
@ApiStatus.AvailableSince("1.0.0")
public interface HelpExecution<C> {
	
	/**
	 * Displays a help menu showing all possible syntaxes
	 *
	 * @param source the source of this execution
	 * @param help   the help object
	 * @param page   the page of the help menu
	 */
	void help(CommandSource<C> source, Context<C> context,
	          CommandHelp<C> help, @Nullable Integer page);
}
