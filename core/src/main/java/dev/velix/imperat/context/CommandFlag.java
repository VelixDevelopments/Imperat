package dev.velix.imperat.context;

import dev.velix.imperat.util.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
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

    static CommandFlag create(String name, List<String> alias, Type inputType) {
        return new CommandFlagImpl(name, alias, inputType);
    }

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
    Type inputType();

    default boolean hasAlias(String alias) {
        return aliases().contains(alias.toLowerCase());
    }

    default String format() {

        String display = this.name();
        String valueFormat = (this instanceof CommandSwitch) ? "" : " <value>";
        return StringUtils.normalizedParameterFormatting("-" + display
            + valueFormat, true);
    }

    default boolean acceptsInput(String input) {
        return this.name().equalsIgnoreCase(input) || hasAlias(input);
    }


    record CommandFlagImpl(String name, List<String> aliases, Type inputType)
        implements CommandFlag {
    }


}
