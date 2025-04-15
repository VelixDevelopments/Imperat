package dev.velix.imperat.resolvers;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a suggestion providing interface
 * for an argument/parameter
 *
 * @param <S> the command-sender valueType
 * @see CommandParameter
 */
@ApiStatus.AvailableSince("1.0.0")
public interface SuggestionResolver<S extends Source> {

    static <S extends Source> SuggestionResolver<S> plain(List<String> results) {
        return ((context, parameterToComplete) -> results);
    }

    static <S extends Source> SuggestionResolver<S> plain(String... results) {
        return plain(Arrays.asList(results));
    }

    static <S extends Source> SuggestionResolver<S> forCommand(Command<S> command) {
        List<String> list = new ArrayList<>();
        list.add(command.name());
        list.addAll(command.aliases());
        return plain(list);
    }


    /**
     * @param context   the context for suggestions
     * @param parameter the parameter of the value to complete
     * @return the auto-completed suggestions of the current argument
     */
    List<String> autoComplete(SuggestionContext<S> context, CommandParameter<S> parameter);

    default CompletableFuture<List<String>> asyncAutoComplete(SuggestionContext<S> context, CommandParameter<S> parameter) {
        return CompletableFuture.supplyAsync(() -> autoComplete(context, parameter));
    }
}
