package dev.velix.imperat.context.internal;

import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.TokenOutOfRangeException;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

final class CommandInputStreamImpl<S extends Source> implements CommandInputStream<S> {

    private final static char WHITE_SPACE = ' ';

    private int letterPos = 0;

    private final Cursor<S> cursor;

    private final ArgumentQueue queue;
    private final CommandUsage<S> usage;

    CommandInputStreamImpl(ArgumentQueue queue, CommandUsage<S> usage) {
        this.queue = queue;
        this.usage = usage;
        this.cursor = new Cursor<>(this, 0, 0);
    }

    @Override
    public @NotNull Cursor<S> cursor() {
        return cursor;
    }

    @Override
    public @NotNull CommandParameter<S> currentParameter() {
        return Optional.ofNullable(usage.getParameter(cursor.parameter))
            .orElseThrow(TokenOutOfRangeException::new);
    }

    @Override
    public Optional<CommandParameter<S>> peekParameter() {
        return Optional.ofNullable(
            usage.getParameter(cursor.parameter + 1)
        );
    }

    @Override
    public Optional<CommandParameter<S>> popParameter() {
        cursor.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
        return Optional.of(currentParameter());
    }

    @Override
    public @NotNull String currentRaw() {
        if (cursor.raw >= queue.size())
            throw new TokenOutOfRangeException();
        return queue.get(cursor.raw);
    }

    @Override
    public @NotNull Character currentLetter() {
        if (cursor.raw >= queue.size()) {
            throw new TokenOutOfRangeException();
        }
        @NotNull String raw = Objects.requireNonNull(currentRaw());
        return raw.charAt(letterPos);
    }

    @Override
    public Optional<Character> peekLetter() {
        int nextLetterPos = letterPos + 1;
        String currentRaw = Objects.requireNonNull(currentRaw());
        if (nextLetterPos == currentRaw.length()) {
            return Optional.of(WHITE_SPACE);
        } else if (nextLetterPos > currentRaw.length()) {
            //next raw
            return peekRaw().map((nextRaw) -> nextRaw.charAt(0));
        } else {
            //nextLetterPos < currentRaw.length()
            return Optional.of(currentRaw.charAt(nextLetterPos));
        }
    }

    @Override
    public Optional<Character> popLetter() {
        letterPos++;
        String currentRaw = Objects.requireNonNull(currentRaw());
        if (letterPos == currentRaw.length()) {
            return Optional.of(WHITE_SPACE);
        } else if (letterPos > currentRaw.length()) {
            //next raw
            letterPos = 0;
            return popRaw().map((nextRaw) -> nextRaw.charAt(0));
        } else {
            //nextLetterPos < currentRaw.length()
            return Optional.of(currentRaw.charAt(letterPos));
        }
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
        return Optional.of(currentRaw());
    }

    @Override
    public boolean hasNextLetter() {
        return Optional.of(currentRaw())
            .map((raw) -> {
                if (letterPos >= raw.length()) {
                    return peekRaw().isPresent();
                }
                return true;
            }).orElse(false);
    }

    @Override
    public boolean hasNextRaw() {
        return cursor.raw < queue.size();
    }

    @Override
    public boolean hasNextParameter() {
        return cursor.parameter < usage.size();
    }

    @Override
    public @NotNull ArgumentQueue getRawQueue() {
        return queue;
    }

    @Override
    public @NotNull CommandUsage<S> getUsage() {
        return usage;
    }

    @Override
    public boolean skipLetter() {
        return popLetter().isPresent();
    }

}
