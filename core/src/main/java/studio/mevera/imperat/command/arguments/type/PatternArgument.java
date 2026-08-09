package studio.mevera.imperat.command.arguments.type;

import org.jetbrains.annotations.NotNull;
import studio.mevera.imperat.command.arguments.Argument;
import studio.mevera.imperat.context.CommandContext;
import studio.mevera.imperat.context.CommandSource;
import studio.mevera.imperat.exception.ArgumentParseException;
import studio.mevera.imperat.exception.CommandException;
import studio.mevera.imperat.responses.ResponseKey;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Parses an input token as a {@link Pattern}, compiled at parse time so
 * malformed regexes fail fast with a useful error message.
 */
public final class PatternArgument<S extends CommandSource> extends SimpleArgumentType<S, Pattern> {

    @Override
    public Pattern parse(@NotNull CommandContext<S> context, @NotNull Argument<S> argument, @NotNull String input)
            throws CommandException {
        try {
            return Pattern.compile(input);
        } catch (PatternSyntaxException ex) {
            throw new ArgumentParseException(ResponseKey.INVALID_REGEX, input)
                    .withPlaceholder("reason", ex.getDescription() == null ? "" : ex.getDescription());
        }
    }
}
