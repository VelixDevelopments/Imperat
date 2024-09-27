package dev.velix.imperat.command.parameters;

import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.context.CommandSwitch;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.TypeSuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class FlagBuilder<S extends Source, T> extends ParameterBuilder<S, CommandFlag> {

    private final Type inputType;
    private final List<String> aliases = new ArrayList<>();
    private OptionalValueSupplier<T> defaultValueSupplier = null;
    private TypeSuggestionResolver<S, T> suggestionResolver;

    private FlagBuilder(String name, Class<T> inputType) {
        super(name, CommandFlag.class, true, false);
        this.inputType = inputType;
    }

    //for switches
    private FlagBuilder(String name) {
        this(name, null);
    }

    public static <S extends Source, T> FlagBuilder<S, T> ofFlag(String name, Class<T> inputType) {
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
            throw new IllegalArgumentException("Flag of type switches, cannot have a default value supplier !");
        }
        this.defaultValueSupplier = valueSupplier;
        return this;
    }

    public FlagBuilder<S, T> suggestForInputValue(TypeSuggestionResolver<S, T> suggestionResolver) {
        if (inputType == null) {
            throw new IllegalArgumentException("Flag of type switches, cannot have a default value supplier !");
        }
        this.suggestionResolver = suggestionResolver;
        return this;
    }

    @Override
    public FlagParameter<S> build() {
        if (inputType != null) {
            CommandFlag flag = CommandFlag.create(name, aliases, inputType);
            return new FlagCommandParameter<>(flag, permission, description, defaultValueSupplier, suggestionResolver);
        } else {
            CommandSwitch commandSwitch = CommandSwitch.create(name, aliases);
            return new FlagCommandParameter<>(commandSwitch, permission, OptionalValueSupplier.of(false), suggestionResolver);
        }
    }

}
