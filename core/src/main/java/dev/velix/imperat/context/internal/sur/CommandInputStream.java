package dev.velix.imperat.context.internal.sur;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface CommandInputStream<S extends Source> {

    @NotNull Cursor<S> cursor();

    CommandParameter<S> currentParameter();

    String currentRaw();

    Character currentLetter();

    Optional<Character> peekLetter();

    Optional<Character> popLetter();

    Optional<String> peekRaw();

    Optional<String> popRaw();

    boolean hasNextLetter();

    boolean hasNextRaw();

    boolean hasNextParameter();

    default boolean skip() {
        final Cursor<S> cursor = cursor();
        int prevRaw = cursor.raw;
        cursor.shift(ShiftTarget.ALL, ShiftOperation.RIGHT);
        return cursor.raw > prevRaw;
    }

    default boolean skipRaw() {
        final Cursor<S> cursor = cursor();
        int prevRaw = cursor.raw;
        cursor.shift(ShiftTarget.RAW_ONLY, ShiftOperation.RIGHT);
        return cursor.raw > prevRaw;
    }

}
