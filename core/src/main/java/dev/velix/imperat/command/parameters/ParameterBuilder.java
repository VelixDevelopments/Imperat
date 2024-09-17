package dev.velix.imperat.command.parameters;

import dev.velix.imperat.command.Description;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.Preconditions;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed class ParameterBuilder<S extends Source, T> permits FlagBuilder {
    
    protected final String name;
    private final TypeWrap<T> type;
    private final boolean optional;
    private final boolean greedy;
    
    protected String permission = null;
    protected Description description = Description.EMPTY;
    private OptionalValueSupplier<T> valueSupplier = null;
    private SuggestionResolver<S, T> suggestionResolver = null;
    
    ParameterBuilder(String name, TypeWrap<T> type, boolean optional, boolean greedy) {
        this.name = name;
        this.type = type;
        this.optional = optional;
        this.greedy = greedy;
    }
    
    
    ParameterBuilder(String name, TypeWrap<T> type, boolean optional) {
        this(name, type, optional, false);
    }
    
    ParameterBuilder(String name, Class<T> type, boolean optional, boolean greedy) {
        this(name, TypeWrap.of(type), optional, greedy);
    }
    
    
    ParameterBuilder(String name, Class<T> type, boolean optional) {
        this(name, type, optional, false);
    }
    
    public ParameterBuilder<S, T> permission(@Nullable String permission) {
        this.permission = permission;
        return this;
    }
    
    public ParameterBuilder<S, T> description(@NotNull Description description) {
        Preconditions.notNull(description, "description");
        this.description = description;
        return this;
    }
    
    public ParameterBuilder<S, T> description(String descValue) {
        return description(Description.of(descValue));
    }
    
    public ParameterBuilder<S, T> defaultValue(OptionalValueSupplier<T> defaultValueSupplier) {
        this.valueSupplier = defaultValueSupplier;
        return this;
    }
    
    public ParameterBuilder<S, T> defaultValue(@Nullable T value) {
        return defaultValue(OptionalValueSupplier.of(value));
    }
    
    public ParameterBuilder<S, T> suggest(SuggestionResolver<S, T> suggestionResolver) {
        this.suggestionResolver = suggestionResolver;
        return this;
    }
    
    public ParameterBuilder<S, T> suggest(String... suggestions) {
        return suggest(SuggestionResolver.plain(type, suggestions));
    }
    
    public CommandParameter build() {
        return CommandParameter.of(
                name, type, permission, description,
                optional, greedy, valueSupplier,
                suggestionResolver
        );
    }
    
}
