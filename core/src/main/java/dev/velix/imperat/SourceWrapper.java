package dev.velix.imperat;

import dev.velix.imperat.context.Source;

public sealed interface SourceWrapper<C> permits Imperat {
	/**
	 * Wraps the sender into a built-in command-sender type
	 *
	 * @param sender the sender's actual value
	 * @return the wrapped command-sender type
	 */
	Source<C> wrapSender(C sender);
	
	/**
	 * Checks whether the type can be a command sender
	 *
	 * @param type the type
	 * @return whether the type can be a command sender
	 */
	boolean canBeSender(Class<?> type);
}
