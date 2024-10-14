package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.resolvers.TypeSuggestionResolver;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseParameterType<S extends Source, T> implements ParameterType<S, T> {

    protected final TypeWrap<T> typeWrap;
    protected final List<String> suggestions = new ArrayList<>();
    protected final TypeSuggestionResolver<S, T> suggestionResolver;

    protected BaseParameterType(TypeWrap<T> typeWrap) {
        this.typeWrap = typeWrap;
        this.suggestionResolver = SuggestionResolver.type(typeWrap, suggestions());
    }

    @Override
    public Type type() {
        return typeWrap.getType();
    }

    @Override
    public Collection<String> suggestions() {
        return suggestions;
    }

    @Override
    public TypeSuggestionResolver<S, T> getSuggestionResolver() {
        return suggestionResolver;
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<S> parameter) {
        return true;
    }

    @Override
    public @NotNull ParameterType<S, T> withSuggestions(String... suggestions) {
        this.suggestions.addAll(List.of(suggestions));
        return this;
    }
}
