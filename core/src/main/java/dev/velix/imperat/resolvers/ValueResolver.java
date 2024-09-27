package dev.velix.imperat.resolvers;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.sur.Cursor;
import dev.velix.imperat.exception.ImperatException;
import org.jetbrains.annotations.ApiStatus;

/**
 * Resolves the argument of a certain parameter
 * converting the parameter into a value for
 * the argument input, just using the context, parameter required and the raw input.
 *
 * @param <S> the type of the command sender/source
 * @param <T> the type of value that the parameter requires
 */
@ApiStatus.AvailableSince("1.0.0")
public interface ValueResolver<S extends Source, T> {

    /**
     * @param context   the context for the command
     * @param parameter the parameter corresponding to the raw
     * @param cursor    the cursor for controlling the position of raws and parameters
     * @param raw       the required raw of the command.
     * @return the resolved output from the input object
     */
    T resolve(
            ExecutionContext<S> context,
            CommandParameter<S> parameter,
            Cursor<S> cursor,
            String raw
    ) throws ImperatException;
}
