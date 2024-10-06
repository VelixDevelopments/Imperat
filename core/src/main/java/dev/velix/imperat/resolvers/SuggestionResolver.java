package dev.velix.imperat.resolvers;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    static <S extends Source> TypeSuggestionResolver<S, Command<S>> forCommand(Command<S> command) {
        List<String> list = new ArrayList<>();
        list.add(command.name());
        list.addAll(command.aliases());
        return type(new TypeWrap<>() {
        }, list);
    }

    static <S extends Source, T> TypeSuggestionResolver<S, T> type(Class<T> type, Collection<String> results) {
        return type(TypeWrap.of(type), results);
    }

    static <S extends Source, T> TypeSuggestionResolver<S, T> type(TypeWrap<T> type, Collection<String> results) {
        return new TypeSuggestionResolver<>() {

            @Override
            public @NotNull TypeWrap<T> getType() {
                return type;
            }

            @Override
            public Collection<String> autoComplete(SuggestionContext<S> context, CommandParameter<S> parameter) {
                return results;
            }
        };
    }

    static <S extends Source, T> TypeSuggestionResolver<S, T> type(Class<T> type, String... results) {
        return type(type, List.of(results));
    }

    static <S extends Source, T> TypeSuggestionResolver<S, T> type(TypeWrap<T> type, String... results) {
        return type(type, List.of(results));
    }

    /**
     * @param context   the context for suggestions
     * @param parameter the parameter of the value to complete
     * @return the auto-completed suggestions of the current argument
     */
    Collection<String> autoComplete(SuggestionContext<S> context, CommandParameter<S> parameter);

    default CompletableFuture<Collection<String>> asyncAutoComplete(SuggestionContext<S> context, CommandParameter<S> parameter) {
        return CompletableFuture.supplyAsync(() -> autoComplete(context, parameter));
    }
}
