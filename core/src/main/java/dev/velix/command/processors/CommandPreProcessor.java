package dev.velix.command.processors;

import dev.velix.Imperat;
import dev.velix.command.CommandUsage;
import dev.velix.context.Context;
import dev.velix.context.Source;
import dev.velix.exception.ImperatException;

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
