package dev.velix.imperat.command.parameters;

import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.command.parameters.type.ParameterTypes;
import dev.velix.imperat.context.FlagData;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandFlag;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;

import java.util.ArrayList;
import java.util.List;

public final class FlagBuilder<S extends Source, T> extends ParameterBuilder<S, CommandFlag> {

    private final ParameterType<S, T> inputType;
    private final List<String> aliases = new ArrayList<>();
    private OptionalValueSupplier<T> defaultValueSupplier = null;
    private SuggestionResolver<S> suggestionResolver;

    private FlagBuilder(String name, ParameterType<S, T> inputType) {
        super(name, ParameterTypes.flag(), true, false);
        this.inputType = inputType;
    }

    //for switches
    private FlagBuilder(String name) {
        this(name, null);
    }

    public static <S extends Source, T> FlagBuilder<S, T> ofFlag(String name, ParameterType<S, T> inputType) {
        return new FlagBuilder<>(name, inputType);
    }

    public static <S extends Source, T> FlagBuilder<S, T> ofSwitch(String name) {
        return new FlagBuilder<>(name);
    }

    public FlagBuilder<S, T> aliases(List<String> aliases) {
        this.aliases.addAll(aliases);
        return this;
    }

    public FlagBuilder<S, T> aliases(String... aliases) {
        this.aliases.addAll(List.of(aliases));
        return this;
    }

    public FlagBuilder<S, T> flagDefaultInputValue(OptionalValueSupplier<T> valueSupplier) {
        if (inputType == null) {
            throw new IllegalArgumentException("Flag of valueType switches, cannot have a default value supplier !");
        }
        this.defaultValueSupplier = valueSupplier;
        return this;
    }

    public FlagBuilder<S, T> suggestForInputValue(SuggestionResolver<S> suggestionResolver) {
        if (inputType == null) {
            throw new IllegalArgumentException("Flag of valueType switches, cannot have a default value supplier !");
        }
        this.suggestionResolver = suggestionResolver;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FlagParameter<S> build() {
        FlagData<S> flag = FlagData.create(name, aliases, inputType);
        if (inputType == null) {
            defaultValueSupplier = (OptionalValueSupplier<T>) OptionalValueSupplier.of(false);
        }
        return new FlagCommandParameter<>(flag, permission, description, defaultValueSupplier, suggestionResolver);
    }

}
