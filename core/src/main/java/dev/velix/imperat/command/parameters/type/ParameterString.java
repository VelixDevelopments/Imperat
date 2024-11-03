package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;

public final class ParameterString<S extends Source> extends BaseParameterType<S, String> {

    private final static char DOUBLE_QUOTE = '"', SINGLE_QUOTE = '\'';

    ParameterString() {
        super(TypeWrap.of(String.class));
    }

    @Override
    public @NotNull String resolve(ExecutionContext<S> context, @NotNull CommandInputStream<S> inputStream) throws ImperatException {
        StringBuilder builder = new StringBuilder();
        final CommandParameter<S> parameter = inputStream.currentParameter().orElse(null);
        if (parameter == null) return builder.toString();

        final Character current = inputStream.currentLetter().orElse(null);
        if (current == null)
            return builder.toString();

        if (!isQuoteChar(current)) {

            builder.append(inputStream.currentRaw().orElse(""));
            if (parameter.isGreedy()) {
                handleGreedy(builder, inputStream);
            }
            ImperatDebugger.debug("Read string input= '%s'", builder.toString());

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
            handleGreedy(builder, inputStream);
        }
        ImperatDebugger.debug("Read string input= '%s'", builder.toString());

        return builder.toString();
    }

    private boolean isQuoteChar(char ch) {
        return ch == DOUBLE_QUOTE || ch == SINGLE_QUOTE;
    }

    private void handleGreedy(StringBuilder builder, CommandInputStream<S> inputStream) {
        builder.append(" ");
        while (inputStream.hasNextRaw()) {
            inputStream.popRaw()
                .ifPresent((raw) -> {
                    builder.append(raw);
                    if (inputStream.peekRaw().isPresent()) {
                        builder.append(" ");
                    }
                });
        }
    }
}
