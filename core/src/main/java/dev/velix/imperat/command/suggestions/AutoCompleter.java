package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.CommandSource;
import dev.velix.imperat.command.Command;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Represents a class that's responsible for
 * Handling all auto-completion process during
 * tab-completion per one command, regardless of the sender type
 */
@ApiStatus.AvailableSince("1.0.0")
public interface AutoCompleter<C> {


	/**
	 * @return The auto-completion command
	 */
	Command<C> getCommand();

	/**
	 * Autocompletes an argument from the whole position of the
	 * argument-raw input
	 *
	 * @param dispatcher the command dispatcher
	 * @param sender     the sender writing the command
	 * @param args       the args for raw input
	 * @return the auto-completed results
	 */
	List<String> autoComplete(CommandDispatcher<C> dispatcher,
	                          CommandSource<C> sender, String[] args);

	/**
	 * Autocompletes an argument from the whole position of the
	 * argument-raw input
	 *
	 * @param dispatcher the command dispatcher
	 * @param sender     the sender of the auto-completion
	 * @param currentArg the arg being completed
	 * @param args       the args for raw input
	 * @return the auto-completed results
	 */
	List<String> autoCompleteArgument(CommandDispatcher<C> dispatcher,
	                                  CommandSource<C> sender, CompletionArg currentArg, String[] args);

	static <C> AutoCompleter<C> createNative(Command<C> command) {
		return new AutoCompleterImpl<>(command);
	}
}
