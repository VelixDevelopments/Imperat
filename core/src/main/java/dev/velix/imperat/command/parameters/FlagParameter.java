package dev.velix.imperat.command.parameters;

import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.context.CommandSwitch;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.TypeSuggestionResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public interface FlagParameter<S extends Source> extends CommandParameter<S> {

    /**
     * @return The flag's data
     */
    @NotNull CommandFlag flagData();

    /**
     * @return The type of input value
     */
    default Type inputValueType() {
        return flagData().inputType();
    }

    /**
     * @param <T> the type of flag input value
     * @return the {@link TypeSuggestionResolver} for input value of this flag
     * null if the flag is {@link CommandSwitch}, check using {@link FlagParameter#isSwitch()}
     */
    @Nullable <T> TypeSuggestionResolver<S, T> inputSuggestionResolver();

    /**
     * @return checks whether this parameter is a flag
     */
    @Override
    default boolean isFlag() {
        return true;
    }

    default boolean isSwitch() {
        return flagData() instanceof CommandSwitch;
    }
}
