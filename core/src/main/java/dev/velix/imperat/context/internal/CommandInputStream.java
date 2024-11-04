package dev.velix.imperat.context.internal;

import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface CommandInputStream<S extends Source> {

    @NotNull Cursor<S> cursor();

    @NotNull Optional<CommandParameter<S>> currentParameter();

    Optional<CommandParameter<S>> peekParameter();

    Optional<CommandParameter<S>> popParameter();

    @NotNull Optional<Character> currentLetter();

    Optional<Character> peekLetter();

    Optional<Character> popLetter();

    @NotNull Optional<String> currentRaw();

    Optional<String> peekRaw();

    Optional<String> popRaw();

    boolean hasNextLetter();

    boolean hasNextRaw();

    boolean hasNextParameter();

    @NotNull ArgumentQueue getRawQueue();

    @NotNull CommandUsage<S> getUsage();

    boolean skip();

    boolean skipLetter();

    default boolean skipRaw() {
        final Cursor<S> cursor = cursor();
        int prevRaw = cursor.raw;
        cursor.shift(ShiftTarget.RAW_ONLY, ShiftOperation.RIGHT);
        return cursor.raw > prevRaw;
    }

    default boolean skipParameter() {
        final Cursor<S> cursor = cursor();
        int prevParam = cursor.parameter;
        cursor.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
        return cursor.parameter > prevParam;
    }

    default int currentRawPosition() {
        return cursor().raw;
    }

    default int currentParameterPosition() {
        return cursor().parameter;
    }


    default int rawsLength() {
        return getRawQueue().size();
    }

    default int parametersLength() {
        return getUsage().size();
    }

    default boolean skipTill(char target) {
        boolean reached = false;

        while (hasNextLetter()) {
            if (currentLetter().map((current) -> current == target)
                .orElse(false)) {
                reached = true;
                break;
            }
            popLetter();
        }
        //skipping current letter (which equals the target)
        skipLetter();
        return reached;

    }

    default String collectBeforeFirst(char c) {
        StringBuilder builder = new StringBuilder();
        while (hasNextLetter()) {
            var current = currentLetter().orElse(null);
            if (current == null || current == c) {
                break;
            }
            builder.append(current);
            skipLetter();
        }
        return builder.toString();
    }

}
