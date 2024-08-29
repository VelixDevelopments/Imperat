package dev.velix.imperat.resolvers;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.suggestions.CompletionArg;
import dev.velix.imperat.context.ArgumentQueue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a suggestion providing interface
 * for an argument/parameter
 *
 * @param <C> the command-sender type
 * @see CommandParameter
 */
@ApiStatus.AvailableSince("1.0.0")
public interface SuggestionResolver<C, T> {

    static <C, T> SuggestionResolver<C, T> plain(Class<T> type, List<String> results) {
        return new SuggestionResolver<>() {
            @Override
            public Class<T> getType() {
                return type;
            }

            @Override
            public List<String> autoComplete(Command<C> command,
                                             C source,
                                             ArgumentQueue queue,
                                             CommandParameter parameterToComplete,
                                             @Nullable CompletionArg argToComplete) {
                return results;
            }
        };
    }

    static <C, T> SuggestionResolver<C, T> plain(Class<T> type, String... results) {
        return plain(type, Arrays.asList(results));
    }

    /**
     * @return Type of data the suggestion is resolving
     */
    Class<T> getType();

    /**
     * @param command             the running command
     * @param source              the sender of the command
     * @param queue               the argument raw input
     * @param parameterToComplete the parameter of the arg to complete
     * @param argToComplete       the current raw argument/input that's being requested
     *                            to complete
     * @return the auto-completed suggestions of the current argument
     */
    List<String> autoComplete(Command<C> command,
                              C source,
                              ArgumentQueue queue,
                              CommandParameter parameterToComplete,
                              @Nullable CompletionArg argToComplete);

}
