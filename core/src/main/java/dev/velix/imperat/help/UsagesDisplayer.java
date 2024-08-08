package dev.velix.imperat.help;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.CommandSource;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Represents how all usages together are organized to be
 * displayed to the command sender.
 * <p>
 * This class is only responsible for organizing the
 * formatted usages NOT for formatting the usages
 * <p>
 * Formatting the usages is the mere responsibility
 * of the class {@link UsageFormatter}
 */
@ApiStatus.AvailableSince("1.0.0")
@FunctionalInterface
public interface UsagesDisplayer {

	/**
	 * Displays all usages together to
	 * the {@link CommandSource}
	 * <p>
	 * It has 2 pre-made implementations
	 * @see PlainDisplayer
	 * @see TreeDisplayer
	 *
	 * @param dispatcher the command dispatcher
	 * @param command the command that owns all the usages
	 * @param source the command sender
	 * @param formatter the formatter {@link UsageFormatter}
	 * @param usages  the usages to display
	 *
	 * @param <C> the command-sender type
	 */
	<C> void display(
			  CommandDispatcher<C> dispatcher,
			  Command<C> command,
			  CommandSource<C> source,
			  UsageFormatter formatter,
			  List<CommandUsage<C>> usages
	);

	static UsagesDisplayer plain() {
		return new PlainDisplayer();
	}

	static UsagesDisplayer tree() {
		return new TreeDisplayer();
	}

	static UsagesDisplayer custom(UsagesDisplayer displayer) {
		return displayer;
	}
}
