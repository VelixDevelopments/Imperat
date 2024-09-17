package dev.velix.resolvers;

import dev.velix.command.parameters.CommandParameter;
import dev.velix.context.ExecutionContext;
import dev.velix.context.Source;
import dev.velix.context.internal.sur.Cursor;
import dev.velix.exception.ImperatException;
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
            CommandParameter parameter,
            Cursor cursor,
            String raw
    ) throws ImperatException;
}
