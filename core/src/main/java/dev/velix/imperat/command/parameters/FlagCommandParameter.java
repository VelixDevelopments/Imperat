package dev.velix.imperat.command.parameters;

import com.google.common.reflect.TypeToken;
import dev.velix.imperat.command.Description;
import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.context.CommandSwitch;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.Internal
public final class FlagCommandParameter extends InputParameter implements FlagParameter {

    private final CommandFlag flag;
    private final OptionalValueSupplier<?> supplier;

    FlagCommandParameter(
            CommandFlag flag,
            String permission,
            Description description,
            OptionalValueSupplier<?> valueSupplier
    ) {
        this(flag.name(), permission, flag.aliases(), description, flag.inputType(), valueSupplier);
    }
    
    FlagCommandParameter(String flagName, @Nullable String permission, List<String> aliases, Description description, Class<?> inputType, OptionalValueSupplier<?> supplier) {
        super(flagName, TypeToken.of(CommandFlag.class), permission, description,
                true, true, false, null, null);
        flag = CommandFlag.create(flagName, aliases, inputType);
        this.supplier = supplier;
    }

    FlagCommandParameter(CommandSwitch commandSwitch, @Nullable String permission,
                         Description description, OptionalValueSupplier<?> supplier) {
        super(commandSwitch.name(), TypeToken.of(CommandSwitch.class), permission, description,
                true, true, false,
                null, null);
        this.flag = commandSwitch;
        this.supplier = supplier;
    }

    FlagCommandParameter(
            CommandSwitch commandSwitch,
            @Nullable String permission,
            OptionalValueSupplier<?> supplier
    ) {
        this(commandSwitch, permission, Description.EMPTY, supplier);
    }

    @Override
    public String format() {
        return flag.format();
    }

    /**
     * @return The flag's data
     */
    @Override
    public @NotNull CommandFlag getFlagData() {
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
}
