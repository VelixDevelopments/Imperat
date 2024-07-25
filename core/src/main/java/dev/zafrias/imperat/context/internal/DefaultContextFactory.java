package dev.zafrias.imperat.context.impl;

import dev.zafrias.imperat.CommandSource;
import dev.zafrias.imperat.context.ArgumentQueue;
import dev.zafrias.imperat.context.Context;
import dev.zafrias.imperat.context.ContextFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class DefaultContextFactory<C> implements ContextFactory<C> {

	/**
	 * @param commandSource the sender/source of this command execution
	 * @param command       the command label used
	 * @param queue         the args input
	 * @return new context from the command and args used by {@link CommandSource}
	 */
	@Override
	public @NotNull Context<C> createContext(@NotNull CommandSource<C> commandSource,
	                                         @NotNull String command,
	                                         @NotNull ArgumentQueue queue) {
		return null;
	}
}
