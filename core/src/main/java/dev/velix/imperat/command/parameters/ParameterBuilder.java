package dev.velix.imperat.command.parameters;

import dev.velix.imperat.command.Description;
import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed class ParameterBuilder<S extends Source, T> permits FlagBuilder {

    protected final String name;
    private final ParameterType<S, T> type;
    private final boolean optional;
    private final boolean greedy;

    protected String permission = null;
    protected Description description = Description.EMPTY;
    private @NotNull OptionalValueSupplier<T> valueSupplier;
    private SuggestionResolver<S> suggestionResolver = null;

    ParameterBuilder(String name, ParameterType<S, T> type, boolean optional, boolean greedy) {
        this.name = name;
        this.type = type;
        this.optional = optional;
        this.greedy = greedy;
        this.valueSupplier = OptionalValueSupplier.empty(type.wrappedType());
    }

    ParameterBuilder(String name, ParameterType<S, T> type, boolean optional) {
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

    public ParameterBuilder<S, T> defaultValue(@NotNull OptionalValueSupplier<T> defaultValueSupplier) {
        this.valueSupplier = defaultValueSupplier;
        return this;
    }

    public ParameterBuilder<S, T> defaultValue(@Nullable T value) {
        return defaultValue(value == null ? OptionalValueSupplier.empty(this.type.wrappedType()) : OptionalValueSupplier.of(value));
    }

    public ParameterBuilder<S, T> suggest(SuggestionResolver<S> suggestionResolver) {
        this.suggestionResolver = suggestionResolver;
        return this;
    }

    public ParameterBuilder<S, T> suggest(String... suggestions) {
        return suggest(SuggestionResolver.plain(suggestions));
    }

    public CommandParameter<S> build() {
        return CommandParameter.of(
            name, type, permission, description,
            optional, greedy, valueSupplier,
            suggestionResolver
        );
    }

}
