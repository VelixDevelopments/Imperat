package dev.velix.imperat.command.parameters.type;

import static dev.velix.imperat.util.StringUtils.isQuoteChar;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;

public final class ParameterString<S extends Source> extends BaseParameterType<S, String> {


    ParameterString() {
        super(TypeWrap.of(String.class));
    }

    @Override
    public @NotNull String resolve(@NotNull ExecutionContext<S> context, @NotNull CommandInputStream<S> inputStream, String input) throws ImperatException {
        StringBuilder builder = new StringBuilder();
        final CommandParameter<S> parameter = inputStream.currentParameter().orElse(null);
        //if (parameter == null) return builder.toString();

        final Character current = inputStream.currentLetter().orElse(null);
        if (current == null)
            return input;

        if (!isQuoteChar(current)) {

            if (parameter != null && parameter.isGreedyString()) {
                handleGreedy(builder, inputStream, input);
            } else {
                builder.append(inputStream.currentRaw().orElse(input));
            }
            return builder.toString();
        }

        Character next;
        do {
            //quoted
            //we shift to next char
            next = inputStream.popLetter().orElse(null);
            if (next == null) break;
            builder.append(next);
        } while (inputStream.hasNextRaw() && inputStream.peekLetter().map((ch) -> !isQuoteChar(ch)).orElse(false));

        return builder.isEmpty() ? input : builder.toString();
    }


    private void handleGreedy(StringBuilder builder, CommandInputStream<S> inputStream, String input) {

        String raw = inputStream.currentRaw().orElse(null);
        //if raw is null OR not equal to the input provided(very rare) ->
        // we are in a case of resolving default value for an optional parameter with no real input from the source.
        if(raw == null || !raw.equals(input)) {
            builder.append(input);
            return;
        }
        //the letter internal cursor is related to the LAST valid raw , that's why, GG
        while (inputStream.hasNextLetter()) {
            inputStream.currentLetter()
                .ifPresent(builder::append);
            inputStream.skipLetter();
        }
    }
}
