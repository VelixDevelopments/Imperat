package dev.velix.imperat.resolvers;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Represents a suggestion providing interface
 * for an argument/parameter
 *
 * @param <S> the command-sender type
 * @see CommandParameter
 */
@ApiStatus.AvailableSince("1.0.0")
public interface SuggestionResolver<S extends Source, T> {
    
    static <S extends Source, T> SuggestionResolver<S, T> plain(Class<T> type, List<String> results) {
        return plain(TypeWrap.of(type), results);
    }
    
    static <S extends Source, T> SuggestionResolver<S, T> plain(TypeWrap<T> type, List<String> results) {
        return new SuggestionResolver<>() {
            @Override
            public TypeWrap<T> getType() {
                return type;
            }
            
            @Override
            public List<String> autoComplete(SuggestionContext<S> context, CommandParameter<S> parameterToComplete) {
                return results;
            }
        };
    }
    
    static <S extends Source, T> SuggestionResolver<S, T> plain(Class<T> type, String... results) {
        return plain(type, List.of(results));
    }
    
    static <S extends Source, T> SuggestionResolver<S, T> plain(TypeWrap<T> type, String... results) {
        return plain(type, List.of(results));
    }
    
    /**
     * @return Type of data the suggestion is resolving
     */
    TypeWrap<T> getType();
    
    /**
     * @param context               the context for suggestions
     * @param parameterToComplete   the parameter of the value to complete
     * @return the auto-completed suggestions of the current argument
     */
    List<String> autoComplete(SuggestionContext<S> context, CommandParameter<S> parameterToComplete);
    
}
