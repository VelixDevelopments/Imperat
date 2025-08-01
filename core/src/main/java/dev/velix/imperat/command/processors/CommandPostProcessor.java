package dev.velix.imperat.command.processors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ImperatException;

/**
 * Defines a functional interface that processes a {@link Context}
 * AFTER the resolving of the arguments into values.
 *
 * @param <S> the command sender valueType
 */
public interface CommandPostProcessor<S extends Source> extends CommandProcessor<S> {

    /**
     * Processes context AFTER the resolving operation.
     *
     * @param imperat the api
     * @param context the context
     * @throws ImperatException the exception to throw if something happens
     */
    void process(
            Imperat<S> imperat,
            ExecutionContext<S> context
    ) throws ImperatException;

}
