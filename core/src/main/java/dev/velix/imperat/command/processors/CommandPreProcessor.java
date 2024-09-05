package dev.velix.imperat.command.processors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exceptions.CommandException;

/**
 * Defines a functional interface that processes a {@link Context}
 * BEFORE the resolving of the arguments into values.
 *
 * @param <S> the command sender type
 */
@FunctionalInterface
public interface CommandPreProcessor<S extends Source> {
    
    /**
     * Processes context BEFORE the resolving operation.
     *
     * @param imperat the api
     * @param command the command
     * @param context the context
     * @param usage   The usage detected
     * @throws CommandException the exception to throw if something happens
     */
    void process(
            Imperat<S> imperat,
            Command<S> command,
            Context<S> context,
            CommandUsage<S> usage
    ) throws CommandException;
    
}
