package dev.velix.imperat.resolvers;

import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a suggestion resolver that has a global valueType
 *
 * @param <S> the source
 * @param <T> the valueType
 */
public interface TypeSuggestionResolver<S extends Source, T> extends SuggestionResolver<S> {

    /**
     * @return the valueType that is specific for these suggestions resolving
     */
    @NotNull
    TypeWrap<T> getType();

}
