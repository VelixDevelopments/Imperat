package dev.velix.imperat.command.processors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.exceptions.CommandException;

/**
 * Defines a functional interface that processes a {@link Context}
 * AFTER the resolving of the arguments into values.
 * @param <C> the command sender type
 */
public interface CommandPostProcessor<C> {
	
	/**
	 * Processes context AFTER the resolving operation.
	 * @param imperat the api
	 * @param command the command
	 * @param context the context
	 * @param usage the usage detected
	 * @throws CommandException the exception to throw if something happens
	 */
	void process(
					Imperat<C> imperat,
					Command<C> command,
					ResolvedContext<C> context,
					CommandUsage<C> usage
	) throws CommandException;
	
}
