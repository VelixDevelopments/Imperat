package dev.velix.imperat.context.internal;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.suggestions.CompletionArg;
import dev.velix.imperat.context.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
final class DefaultContextFactory<S extends Source> extends ContextFactory<S> {
	
	
	DefaultContextFactory(Imperat<S> imperat) {
		super(imperat);
	}
	
	/**
	 * @param source  the sender/source of this command execution
	 * @param command the command label used
	 * @param queue   the args input
	 * @return new context from the command and args used by {@link Source}
	 */
	@Override
	public @NotNull Context<S> createContext(
		@NotNull S source,
		@NotNull Command<S> command,
		@NotNull ArgumentQueue queue
	) {
		return new ContextImpl<>(imperat, command, source, queue);
	}
	
	@Override
	public SuggestionContext<S> createSuggestionContext(
		@NotNull S source,
		@NotNull Command<S> command,
		@NotNull ArgumentQueue queue,
		@NotNull CompletionArg arg
	) {
		return new SuggestionContextImpl<>(imperat, command, source, queue, arg);
	}
	
	/**
	 * @param plainContext the context plain
	 * @return the context after resolving args into values for
	 * later on parsing it into the execution
	 */
	@Override
	public ResolvedContext<S> createResolvedContext(
		@NotNull Context<S> plainContext,
		@NotNull CommandUsage<S> usage
	) {
		return new ResolvedContextImpl<>(
			imperat,
			plainContext,
			usage
		);
	}
}
