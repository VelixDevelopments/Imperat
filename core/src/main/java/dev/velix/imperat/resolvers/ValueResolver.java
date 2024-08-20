package dev.velix.imperat.resolvers;

import dev.velix.imperat.CommandSource;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.exceptions.CommandException;
import org.jetbrains.annotations.ApiStatus;

/**
 * Resolves the argument of a certain parameter
 * converting the parameter into a value without the need for
 * the argument input, just using the context and finally converting
 * the usage parameter into an actual value using the context
 *
 * @param <C> the type of the command sender/source
 * @param <T> the type of value that the parameter requires
 */
@ApiStatus.AvailableSince("1.0.0")
public interface ValueResolver<C, T> {

    /**
     * @param source  the source of the command
     * @param context the context for the command
     * @param raw     the required parameter of the command.
     * @return the resolved output from the input object
     */
    T resolve(CommandSource<C> source, Context<C> context, String raw) throws CommandException;
}
