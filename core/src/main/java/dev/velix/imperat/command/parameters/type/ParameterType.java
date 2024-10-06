package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.sur.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.resolvers.TypeSuggestionResolver;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Collection;

public interface ParameterType<S extends Source, T> {

    Type type();

    @Nullable T resolve(ResolvedContext<S> context, @NotNull CommandInputStream<S> commandInputStream) throws ImperatException;

    Collection<String> suggestions();

    TypeSuggestionResolver<S, T> getSuggestionResolver();

    boolean matchesInput(String input);

    default boolean isRelatedToType(Type type) {
        return TypeUtility.areRelatedTypes(type, this.type());
    }

    default boolean equalsExactly(Type type) {
        return TypeUtility.matches(type, type());
    }

    @NotNull ParameterType<S, T> withSuggestions(String... suggestions);

    @SuppressWarnings("unchecked")
    default TypeWrap<T> wrappedType() {
        return (TypeWrap<T>) TypeWrap.of(type());
    }
}
