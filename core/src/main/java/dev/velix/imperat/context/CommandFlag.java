package dev.velix.imperat.context;

import dev.velix.imperat.util.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a flag that has been parsed/read/loaded from a
 * context of a command entered by the {@link Source}
 * <p>
 * A flag has merely 2 types:
 * 1) A true flag is a flag that has an input value next to it in the raw input
 * 2) A {@link CommandSwitch}
 */
@ApiStatus.AvailableSince("1.0.0")
public interface CommandFlag {

    /**
     * The main name of the flag
     *
     * @return the name(unique) of the flag
     */
    @NotNull
    String name();

    /**
     * @return the alias of the flag
     */
    @NotNull
    List<String> aliases();

    /**
     * @return the type of input
     * from the flag
     */
    Class<?> inputType();

    default boolean hasAlias(String alias) {
        return aliases().contains(alias.toLowerCase());
    }

    static CommandFlag create(String name, List<String> alias, Class<?> inputType) {
        return new CommandFlagImpl(name, alias, inputType);
    }

    default String format() {

        String display = this.name();
        String valueFormat = (this instanceof CommandSwitch) ? "" : " <value>";
        return StringUtils.normalizedParameterFormatting("-" + display
                + valueFormat, true);
    }


    record CommandFlagImpl(String name, List<String> aliases, Class<?> inputType)
            implements CommandFlag {
    }
    

}
