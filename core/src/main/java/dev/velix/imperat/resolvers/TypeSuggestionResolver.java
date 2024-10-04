package dev.velix.imperat.resolvers;

import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a suggestion resolver that has a global type
 *
 * @param <S> the source
 * @param <T> the type
 */
public interface TypeSuggestionResolver<S extends Source, T> extends SuggestionResolver<S> {

    /**
     * @return the type that is specific for these suggestions resolving
     */
    @NotNull
    TypeWrap<T> getType();

}
