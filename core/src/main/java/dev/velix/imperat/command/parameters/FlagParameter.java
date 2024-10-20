package dev.velix.imperat.command.parameters;

import dev.velix.imperat.context.FlagData;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.SuggestionResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public interface FlagParameter<S extends Source> extends CommandParameter<S> {

    /**
     * @return The flag's data
     */
    @NotNull
    FlagData<S> flagData();

    /**
     * @return The valueType of input value
     */
    default Type inputValueType() {
        var type = flagData().inputType();
        if (type == null)
            return Boolean.class;
        return type.type();
    }

    /**
     * @param <T> the valueType of flag input value
     * @return the {@link SuggestionResolver} for input value of this flag
     * null if the flag is switch, check using {@link FlagParameter#isSwitch()}
     */
    @Nullable
    <T> SuggestionResolver<S> inputSuggestionResolver();

    /**
     * @return checks whether this parameter is a flag
     */
    @Override
    default boolean isFlag() {
        return true;
    }

    default boolean isSwitch() {
        return flagData().inputType() == null;
    }
}
