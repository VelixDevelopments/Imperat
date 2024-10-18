package dev.velix.imperat.selector.field.operators;

import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.selector.field.NumericField;
import dev.velix.imperat.util.TypeWrap;
import org.bukkit.entity.Entity;

import java.util.List;

public final class LimitOperatorField extends OperatorField<Integer> {

    private final NumericField<Integer> numericField;

    LimitOperatorField(String name) {
        super(name, TypeWrap.of(Integer.class));
        this.numericField = NumericField.integerField(name);
    }

    /**
     * Parses the given string representation of the value and converts it into the field's value type.
     *
     * @param value the string representation of the value to be parsed
     * @return the parsed value of the field's type
     * @throws ImperatException if the parsing fails
     */
    @Override
    public Integer parseFieldValue(String value) throws ImperatException {
        return numericField.parseFieldValue(value);
    }

    /**
     * Performs an operation on the specified value and a list of entities.
     *
     * @param value    the value to be operated on
     * @param entities the list of entities to be processed
     */
    @Override
    public void operate(Integer value, List<Entity> entities) {
        if (entities.size() < value) return;

        int diff = entities.size() - value;
        for (int i = 0; i < diff; i++)
            entities.remove(entities.size() - 1);

    }
}
