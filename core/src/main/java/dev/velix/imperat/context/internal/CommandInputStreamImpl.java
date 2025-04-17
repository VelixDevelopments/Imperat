package dev.velix.imperat.context.internal;

import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

final class CommandInputStreamImpl<S extends Source> implements CommandInputStream<S> {

    private final static char WHITE_SPACE = ' ';

    private int letterPos = 0;
    private final String inputLine;

    private final Cursor<S> cursor;

    private final ArgumentQueue queue;
    private final List<CommandParameter<S>> parametersList;

    CommandInputStreamImpl(ArgumentQueue queue, CommandUsage<S> parametersList) {
        this(queue, parametersList.getParameters());

    }

    CommandInputStreamImpl(ArgumentQueue queue, List<CommandParameter<S>> parameters) {
        this.queue = queue;
        this.inputLine = queue.getOriginalRaw();
        this.parametersList = parameters;
        this.cursor = new Cursor<>(this, 0, 0);
    }

    @Override
    public @NotNull Cursor<S> cursor() {
        return cursor;
    }

    @Override
    public @NotNull Optional<CommandParameter<S>> currentParameter() {
        if(cursor.parameter >= parametersList.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(parametersList.get(cursor.parameter));
    }

    @Override
    public Optional<CommandParameter<S>> peekParameter() {
        if(cursor.parameter+1 >= parametersList.size())
            return Optional.empty();

        return Optional.ofNullable(
            parametersList.get(cursor.parameter + 1)
        );
    }

    @Override
    public Optional<CommandParameter<S>> popParameter() {
        cursor.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
        return currentParameter();
    }

    @Override
    public @NotNull Optional<String> currentRaw() {
        if (cursor.raw >= queue.size())
            return Optional.empty();
        return Optional.of(queue.get(cursor.raw));
    }

    @Override
    public @NotNull Optional<Character> currentLetter() {
        if (letterPos >= inputLine.length()) {
            return Optional.empty();
        }
        return Optional.of(inputLine.charAt(letterPos));
    }

    @Override
    public Optional<Character> peekLetter() {
        int nextLetterPos = letterPos + 1;
        if (nextLetterPos >= inputLine.length()) return Optional.empty();
        return Optional.of(inputLine.charAt(nextLetterPos));
    }

    @Override
    public Optional<Character> popLetter() {
        letterPos++;
        return currentLetter();
    }

    @Override
    public Optional<String> peekRaw() {
        int next = cursor.raw + 1;

        return Optional.ofNullable(
            queue.getOr(next, null)
        );
    }

    @Override
    public Optional<String> popRaw() {
        cursor.shift(ShiftTarget.RAW_ONLY, ShiftOperation.RIGHT);
        return currentRaw().stream().peek((raw) -> {
            letterPos += raw.length() + 1; //we shift the raw length + the white space
        }).findFirst();
    }

    @Override
    public boolean hasNextLetter() {
        return letterPos < inputLine.length();
    }

    @Override
    public boolean hasNextRaw() {
        return cursor.raw < queue.size();
    }

    @Override
    public boolean hasNextParameter() {
        return cursor.parameter < parametersList.size();
    }

    @Override
    public @NotNull ArgumentQueue getRawQueue() {
        return queue;
    }

    @Override
    public @NotNull List<CommandParameter<S>> getParametersList() {
        return parametersList;
    }

    @Override
    public boolean skip() {
        final Cursor<S> cursor = cursor();
        int prevRaw = cursor.raw;
        cursor.shift(ShiftTarget.ALL, ShiftOperation.RIGHT);

        var currentRaw = currentRaw().orElse(null);
        if (currentRaw == null) return false;

        int diff = inputLine.indexOf(WHITE_SPACE, letterPos) + 1 - letterPos;
        letterPos += diff;
        return cursor.raw > prevRaw;
    }

    @Override
    public boolean skipLetter() {
        return popLetter().isPresent();
    }

}
