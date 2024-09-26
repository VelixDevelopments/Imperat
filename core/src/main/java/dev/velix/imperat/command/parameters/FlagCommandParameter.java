package dev.velix.imperat.command.parameters;

import dev.velix.imperat.command.Description;
import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.context.CommandSwitch;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.TypeSuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;

@ApiStatus.Internal
public final class FlagCommandParameter<S extends Source> extends InputParameter<S> implements FlagParameter<S> {
    
    private final CommandFlag flag;
    private final OptionalValueSupplier<?> supplier;
    private final TypeSuggestionResolver<S, ?> inputValueSuggestionResolver;
    
    FlagCommandParameter(
            CommandFlag flag,
            String permission,
            Description description,
            OptionalValueSupplier<?> valueSupplier,
            TypeSuggestionResolver<S, ?> inputValueSuggestionResolver
    ) {
        this(flag.name(), permission, flag.aliases(), description, flag.inputType(), valueSupplier, inputValueSuggestionResolver);
    }
    
    FlagCommandParameter(
            String flagName,
            @Nullable String permission,
            List<String> aliases,
            Description description,
            Type inputType,
            OptionalValueSupplier<?> supplier,
            TypeSuggestionResolver<S, ?> inputValueSuggestionResolver
    ) {
        super(flagName, TypeWrap.of(CommandFlag.class), permission, description,
                true, true, false, null, null);
        flag = CommandFlag.create(flagName, aliases, inputType);
        this.supplier = supplier;
        this.inputValueSuggestionResolver = inputValueSuggestionResolver;
    }
    
    FlagCommandParameter(CommandSwitch commandSwitch, @Nullable String permission,
                         Description description, OptionalValueSupplier<?> supplier, TypeSuggestionResolver<S, ?> inputValueSuggestionResolver) {
        super(commandSwitch.name(), TypeWrap.of(CommandSwitch.class), permission, description,
                true, true, false,
                null, null);
        this.flag = commandSwitch;
        this.supplier = supplier;
        this.inputValueSuggestionResolver = inputValueSuggestionResolver;
    }
    
    FlagCommandParameter(
            CommandSwitch commandSwitch,
            @Nullable String permission,
            OptionalValueSupplier<?> supplier,
            TypeSuggestionResolver<S, ?> suggestionResolver
    ) {
        this(commandSwitch, permission, Description.EMPTY, supplier, suggestionResolver);
    }
    
    @Override
    public String format() {
        return flag.format();
    }
    
    /**
     * @return The flag's data
     */
    @Override
    public @NotNull CommandFlag flagData() {
        return flag;
    }
    
    /**
     * @return the default value if it's input is not present
     * in case of the parameter being optional
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> OptionalValueSupplier<T> getDefaultValueSupplier() {
        return (OptionalValueSupplier<T>) supplier;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <T> TypeSuggestionResolver<S, T> inputSuggestionResolver() {
        if (isSwitch())
            return null;
        else
            return (TypeSuggestionResolver<S, T>) inputValueSuggestionResolver;
    }
}
