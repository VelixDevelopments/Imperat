package dev.velix.imperat.resolvers;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.sur.Cursor;
import dev.velix.imperat.exceptions.CommandException;
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
     * @param source    the source of the command
     * @param context   the context for the command
     * @param raw       the required raw of the command.
     * @param cursor    the cursor for controlling the position of raws and parameters
     * @param parameter the parameter corresponding to the raw
     * @return the resolved output from the input object
     */
    T resolve(
            S source,
            Context<S> context,
            String raw,
            Cursor cursor,
            CommandParameter parameter
    ) throws CommandException;
}
