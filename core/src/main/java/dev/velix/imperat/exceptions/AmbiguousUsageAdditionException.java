package dev.velix.imperat.exceptions;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.command.CommandUsage;

public final class AmbiguousUsageAdditionException extends RuntimeException {

	public <C> AmbiguousUsageAdditionException(
			  Command<C> command,
			  CommandDispatcher<C> dispatcher,
			  CommandUsage<C> first,
			  CommandUsage<C> second
	) {
		super(
				  String.format(
				   "Failed to add usage '%s' because it's ambiguous along with other usage '%s'",
				   CommandUsage.format(command, first),
				   CommandUsage.format(command, second)
				  )
		);
	}


}
