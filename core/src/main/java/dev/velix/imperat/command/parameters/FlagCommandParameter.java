package dev.velix.imperat.command.parameters;

import dev.velix.imperat.command.Description;
import dev.velix.imperat.command.parameters.type.ParameterTypes;
import dev.velix.imperat.context.FlagData;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.SuggestionResolver;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class FlagCommandParameter<S extends Source> extends InputParameter<S> implements FlagParameter<S> {

    private final FlagData<S> flag;
    private final OptionalValueSupplier inputValueSupplier;
    private final SuggestionResolver<S> inputValueSuggestionResolver;

    FlagCommandParameter(
        FlagData<S> flag,
        String permission,
        Description description,
        OptionalValueSupplier inputValueSupplier,
        SuggestionResolver<S> inputValueSuggestionResolver
    ) {
        super(
            flag.name(), ParameterTypes.flag(flag),
            permission, description,
            true, true, false,
            OptionalValueSupplier.empty(),
            inputValueSuggestionResolver
        );
        this.flag = flag;
        this.inputValueSupplier = inputValueSupplier;
        this.inputValueSuggestionResolver = inputValueSuggestionResolver;
    }

    @Override
    public String format() {
        return flag.format();
    }

    /**
     * @return The flag's data
     */
    @Override
    public @NotNull FlagData<S> flagData() {
        return flag;
    }

    /**
     * @return the default value if it's input is not present
     * in case of the parameter being optional
     */
    public @NotNull OptionalValueSupplier getDefaultValueSupplier() {
        return inputValueSupplier;
    }

    @Override
    public @Nullable SuggestionResolver<S> inputSuggestionResolver() {
        if (isSwitch())
            return null;
        else
            return inputValueSuggestionResolver;
    }
}
