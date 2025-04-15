package dev.velix.imperat.command.parameters.type;

import static dev.velix.imperat.util.StringUtils.isQuoteChar;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ParameterString<S extends Source> extends BaseParameterType<S, String> {


    ParameterString() {
        super(TypeWrap.of(String.class));
    }

    @Override
    public @NotNull String resolve(@NotNull ExecutionContext<S> context, @NotNull CommandInputStream<S> inputStream) throws ImperatException {
        StringBuilder builder = new StringBuilder();
        final CommandParameter<S> parameter = inputStream.currentParameter().orElse(null);
        //if (parameter == null) return builder.toString();

        final Character current = inputStream.currentLetter().orElse(null);
        if (current == null)
            return builder.toString();

        if (!isQuoteChar(current)) {

            if (parameter != null && parameter.isGreedyString()) {
                handleGreedy(builder, inputStream);
            } else {
                builder.append(inputStream.currentRaw().orElse(""));
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

        return builder.toString();
    }

    @Override
    public @Nullable String fromString(Imperat<S> imperat, String input) throws ImperatException {
        return input;
    }


    private void handleGreedy(StringBuilder builder, CommandInputStream<S> inputStream) {
        var raw = inputStream.currentRaw().orElse(null);
        if (raw == null) return;

        while (inputStream.hasNextLetter()) {
            inputStream.currentLetter()
                .ifPresent((builder::append));
            inputStream.skipLetter();
        }
    }
}
