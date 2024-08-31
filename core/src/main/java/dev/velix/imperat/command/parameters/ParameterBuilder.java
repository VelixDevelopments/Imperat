package dev.velix.imperat.command.parameters;

import dev.velix.imperat.command.Description;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed class ParameterBuilder<C, T> permits FlagBuilder {
	
	protected final String name;
	private final Class<T> type;
	private final boolean optional;
	private final boolean greedy;
	
	protected Description description = Description.EMPTY;
	private OptionalValueSupplier<T> valueSupplier = null;
	private SuggestionResolver<C, T> suggestionResolver = null;
	
	ParameterBuilder(String name, Class<T> type, boolean optional, boolean greedy) {
		this.name = name;
		this.type = type;
		this.optional = optional;
		this.greedy = greedy;
	}
	
	ParameterBuilder(String name, Class<T> type, boolean optional) {
		this(name, type, optional, false);
	}

	
	public ParameterBuilder<C, T> description(@NotNull Description description) {
		Preconditions.notNull(description, "description cannot be null !");
		this.description = description;
		return this;
	}
	
	public ParameterBuilder<C, T> description(String descValue) {
		return description(Description.of(descValue));
	}
	
	public ParameterBuilder<C, T> defaultValue(OptionalValueSupplier<T> defaultValueSupplier) {
		this.valueSupplier = defaultValueSupplier;
		return this;
	}
	
	public ParameterBuilder<C, T> defaultValue(@Nullable T value) {
		return defaultValue(OptionalValueSupplier.of(value));
	}
	
	public ParameterBuilder<C, T> suggest(SuggestionResolver<C, T> suggestionResolver) {
		this.suggestionResolver = suggestionResolver;
		return this;
	}
	
	public ParameterBuilder<C, T> suggest(String... suggestions) {
		return suggest(SuggestionResolver.plain(type, suggestions));
	}
	
	public CommandParameter build() {
		return CommandParameter.of(
						name, type, description,
						optional, greedy, valueSupplier,
						suggestionResolver
		);
	}
	
}
