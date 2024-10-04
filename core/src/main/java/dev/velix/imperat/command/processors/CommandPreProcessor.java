package dev.velix.imperat.command.processors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ImperatException;

/**
 * Defines a functional interface that processes a {@link Context}
 * BEFORE the resolving of the arguments into values.
 *
 * @param <S> the command sender type
 */
@FunctionalInterface
public interface CommandPreProcessor<S extends Source> extends CommandProcessor {
	
	/**
	 * Processes context BEFORE the resolving operation.
	 *
	 * @param imperat the api
	 * @param context the context
	 * @param usage   The usage detected
	 * @throws ImperatException the exception to throw if something happens
	 */
	void process(
		Imperat<S> imperat,
		Context<S> context,
		CommandUsage<S> usage
	) throws ImperatException;
	
}
