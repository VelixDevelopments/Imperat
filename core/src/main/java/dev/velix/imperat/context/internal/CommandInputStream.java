package dev.velix.imperat.context.internal;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Represents a stream of command input, allowing navigation and manipulation
 * of raw input, parameters, and individual characters.
 *
 * @param <S> The type of the source associated with the command input stream.
 */
public interface CommandInputStream<S extends Source> {

    /**
     * Retrieves the current cursor position in the stream.
     *
     * @return The current cursor.
     */
    @NotNull Cursor<S> cursor();

    /**
     * Retrieves the current command parameter at the cursor position.
     *
     * @return An {@link Optional} containing the current command parameter, or empty if none exists.
     */
    @NotNull Optional<CommandParameter<S>> currentParameter();

    /**
     * Peeks at the next command parameter without advancing the cursor.
     *
     * @return An {@link Optional} containing the next command parameter, or empty if none exists.
     */
    Optional<CommandParameter<S>> peekParameter();

    /**
     * Pops the current command parameter and advances the cursor.
     *
     * @return An {@link Optional} containing the popped command parameter, or empty if none exists.
     */
    Optional<CommandParameter<S>> popParameter();

    /**
     * Retrieves the previous command parameter without moving the cursor.
     *
     * @return An {@link Optional} containing the previous command parameter, or empty if none exists.
     */
    Optional<CommandParameter<S>> prevParameter();

    /**
     * Retrieves the current character at the cursor position.
     *
     * @return An {@link Optional} containing the current character, or empty if none exists.
     */
    @NotNull Optional<Character> currentLetter();

    /**
     * Peeks at the next character without advancing the cursor.
     *
     * @return An {@link Optional} containing the next character, or empty if none exists.
     */
    Optional<Character> peekLetter();

    /**
     * Pops the current character and advances the cursor.
     *
     * @return An {@link Optional} containing the popped character, or empty if none exists.
     */
    Optional<Character> popLetter();

    /**
     * Retrieves the current raw input at the cursor position.
     *
     * @return An {@link Optional} containing the current raw input, or empty if none exists.
     */
    @NotNull Optional<String> currentRaw();

    /**
     * Peeks at the next raw input without advancing the cursor.
     *
     * @return An {@link Optional} containing the next raw input, or empty if none exists.
     */
    Optional<String> peekRaw();

    /**
     * Pops the current raw input and advances the cursor.
     *
     * @return An {@link Optional} containing the popped raw input, or empty if none exists.
     */
    Optional<String> popRaw();

    /**
     * Retrieves the previous raw input without moving the cursor.
     *
     * @return An {@link Optional} containing the previous raw input, or empty if none exists.
     */
    Optional<String> prevRaw();

    /**
     * Checks if there is a next character available in the stream.
     *
     * @return True if a next character exists, false otherwise.
     */
    boolean hasNextLetter();

    /**
     * Checks if there is a next raw input available in the stream.
     *
     * @return True if a next raw input exists, false otherwise.
     */
    boolean hasNextRaw();

    /**
     * Checks if there is a previous raw input available in the stream.
     *
     * @return True if a previous raw input exists, false otherwise.
     */
    boolean hasPreviousRaw();

    /**
     * Checks if there is a next command parameter available in the stream.
     *
     * @return True if a next command parameter exists, false otherwise.
     */
    boolean hasNextParameter();

    /**
     * Checks if there is a previous command parameter available in the stream.
     *
     * @return True if a previous command parameter exists, false otherwise.
     */
    boolean hasPreviousParameter();

    /**
     * Retrieves the queue of raw inputs.
     *
     * @return The {@link ArgumentQueue} containing raw inputs.
     */
    @NotNull ArgumentQueue getRawQueue();

    /**
     * Retrieves the list of command parameters.
     *
     * @return A {@link List} of command parameters.
     */
    @NotNull List<CommandParameter<S>> getParametersList();

    /**
     * Skips the current input (raw or parameter) and advances the cursor.
     *
     * @return True if the skip was successful, false otherwise.
     */
    boolean skip();

    /**
     * Skips the current character and advances the cursor.
     *
     * @return True if the skip was successful, false otherwise.
     */
    boolean skipLetter();

    /**
     * Skips the current raw input and advances the cursor.
     *
     * @return True if the skip was successful, false otherwise.
     */
    default boolean skipRaw() {
        final Cursor<S> cursor = cursor();
        int prevRaw = cursor.raw;
        cursor.shift(ShiftTarget.RAW_ONLY, ShiftOperation.RIGHT);
        return cursor.raw > prevRaw;
    }

    /**
     * Skips the current command parameter and advances the cursor.
     *
     * @return True if the skip was successful, false otherwise.
     */
    default boolean skipParameter() {
        final Cursor<S> cursor = cursor();
        int prevParam = cursor.parameter;
        cursor.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
        return cursor.parameter > prevParam;
    }

    /**
     * Retrieves the current position of the raw input cursor.
     *
     * @return The current raw input position.
     */
    default int currentRawPosition() {
        return cursor().raw;
    }

    /**
     * Retrieves the current position of the parameter cursor.
     *
     * @return The current parameter position.
     */
    default int currentParameterPosition() {
        return cursor().parameter;
    }

    /**
     * Retrieves the total number of raw inputs in the stream.
     *
     * @return The number of raw inputs.
     */
    default int rawsLength() {
        return getRawQueue().size();
    }

    /**
     * Retrieves the total number of command parameters in the stream.
     *
     * @return The number of command parameters.
     */
    default int parametersLength() {
        return getParametersList().size();
    }

    /**
     * Skips characters in the stream until the specified target character is reached.
     *
     * @param target The target character to skip to.
     * @return True if the target character was reached, false otherwise.
     */
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
        // Skipping current letter (which equals the target)
        skipLetter();
        return reached;
    }

    /**
     * Collects all characters before the first occurrence of the specified character.
     *
     * @param c The character to stop collecting at.
     * @return A string containing the collected characters.
     */
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

    /**
     * @return A copy of {@link CommandInputStream}
     */
    CommandInputStream<S> copy();

    /**
     * Reads the current raw input.
     *
     * @return The current raw input.
     * @throws NoSuchElementException if no raw input is available.
     */
    default String readInput() {
        return currentRaw().orElseThrow();
    }

    /**
     * Creates a new {@link CommandInputStream} with a single string as input.
     *
     * @param parameter The command parameter associated with the input.
     * @param str       The raw input string.
     * @param <S>       The type of the source.
     * @return A new {@link CommandInputStream} instance.
     */
    static <S extends Source> CommandInputStream<S> ofSingleString(@NotNull CommandParameter<S> parameter, @NotNull String str) {
        return new CommandInputStreamImpl<>(ArgumentQueue.of(str), List.of(parameter));
    }

    /**
     * Creates a substream of the current {@link CommandInputStream} with the specified input.
     *
     * @param stream The parent command input stream.
     * @param input  The raw input for the substream.
     * @param <S>    The type of the source.
     * @return A new {@link CommandInputStream} instance representing the substream.
     * @throws NoSuchElementException if the current parameter is not available.
     */
    static <S extends Source> CommandInputStream<S> subStream(@NotNull CommandInputStream<S> stream, @NotNull String input) {
        return ofSingleString(stream.currentParameter().orElseThrow(), input);
    }
}