package studio.mevera.imperat.command.arguments.type;

import org.jetbrains.annotations.NotNull;
import studio.mevera.imperat.command.arguments.Argument;
import studio.mevera.imperat.context.CommandContext;
import studio.mevera.imperat.context.CommandSource;
import studio.mevera.imperat.exception.ArgumentParseException;
import studio.mevera.imperat.exception.CommandException;
import studio.mevera.imperat.responses.ResponseKey;

import java.math.BigInteger;

/**
 * Parses an input token into an arbitrary-precision {@link BigInteger}.
 * Useful for IDs and counters that exceed {@code long} range.
 */
public final class BigIntegerArgument<S extends CommandSource> extends SimpleArgumentType<S, BigInteger> {

    @Override
    public BigInteger parse(@NotNull CommandContext<S> context, @NotNull Argument<S> argument, @NotNull String input)
            throws CommandException {
        try {
            return new BigInteger(input);
        } catch (NumberFormatException ex) {
            throw new ArgumentParseException(ResponseKey.INVALID_INTEGER, input);
        }
    }
}
