package dev.velix.imperat.context.internal;

import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface CommandInputStream<S extends Source> {

    @NotNull Cursor<S> cursor();

    @Nullable CommandParameter<S> currentParameter();

    Optional<CommandParameter<S>> peekParameter();

    Optional<CommandParameter<S>> popParameter();

    @Nullable Character currentLetter();

    Optional<Character> peekLetter();

    Optional<Character> popLetter();

    @Nullable String currentRaw();

    Optional<String> peekRaw();

    Optional<String> popRaw();

    boolean hasNextLetter();

    boolean hasNextRaw();

    boolean hasNextParameter();

    @NotNull ArgumentQueue getRawQueue();

    @NotNull CommandUsage<S> getUsage();

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


}
