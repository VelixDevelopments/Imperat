package dev.velix.imperat.selector.field.filters;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.selector.EntityCondition;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;

final class NameField extends PredicateField<String> {
    /**
     * Constructs an AbstractField instance with the specified name and type.
     *
     * @param name The name of the selection field.
     */
    NameField(String name) {
        super(name, TypeWrap.of(String.class));
    }

    @Override
    protected @NotNull EntityCondition getCondition(String value, CommandInputStream<BukkitSource> commandInputStream) {
        return (sender, entity) -> entity.getName().equalsIgnoreCase(value);
    }

    /**
     * Parses the given string representation of the value and converts it into the field's value type.
     *
     * @param value the string representation of the value to be parsed
     * @return the parsed value of the field's type
     */
    @Override
    public String parseFieldValue(String value) {
        return value;
    }

}
