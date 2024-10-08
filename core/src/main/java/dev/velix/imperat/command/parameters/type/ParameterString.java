package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ParameterString<S extends Source> extends BaseParameterType<S, String> {

    private final static char DOUBLE_QUOTE = '"', SINGLE_QUOTE = '\'';

    ParameterString() {
        super(TypeWrap.of(String.class));
    }

    @Override
    public @Nullable String resolve(ExecutionContext<S> context, @NotNull CommandInputStream<S> inputStream) throws ImperatException {
        StringBuilder builder = new StringBuilder();
        final CommandParameter<S> parameter = inputStream.currentParameter();
        assert parameter != null;

        final Character current = inputStream.currentLetter();
        if (current == null) return null;

        if (!isQuoteChar(current)) {
            builder.append(inputStream.currentRaw());

            if (parameter.isGreedy()) {
                while (inputStream.hasNextRaw()) {
                    inputStream.popRaw()
                        .ifPresent(builder::append);
                }
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

        } while (inputStream.hasNextLetter() &&
            inputStream.peekLetter().filter((ch) -> !isQuoteChar(ch)).isPresent());

        if (parameter.isGreedy()) {
            while (inputStream.hasNextRaw()) {
                inputStream.popRaw()
                    .ifPresent(builder::append);
            }
        }

        return builder.toString();
    }

    private boolean isQuoteChar(char ch) {
        return ch == DOUBLE_QUOTE || ch == SINGLE_QUOTE;
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<S> parameter) {
        return true;
    }
}
