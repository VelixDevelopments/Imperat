package dev.velix.imperat.selector.field.filters;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.selector.EntityCondition;
import dev.velix.imperat.selector.field.NumericField;
import dev.velix.imperat.selector.field.Range;
import dev.velix.imperat.selector.field.RangedNumericField;
import dev.velix.imperat.util.TypeWrap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class LevelField extends PredicateField<Range<Integer>> {

    private final RangedNumericField<Integer> numericField;

    LevelField(String name) {
        super(name, new TypeWrap<>() {
        });
        this.numericField = RangedNumericField.of(NumericField.integerField(name));
    }


    @Override
    protected @NotNull EntityCondition getCondition(Range<Integer> value, CommandInputStream<BukkitSource> commandInputStream) {
        return ((sender, entity) -> {
            if (!(entity instanceof Player humanEntity)) return false;
            return value.isInRange(humanEntity.getLevel());
        });
    }

    /**
     * Parses the given string representation of the value and converts it into the field's value type.
     *
     * @param value the string representation of the value to be parsed
     * @return the parsed value of the field's type
     * @throws ImperatException if the parsing fails
     */
    @Override
    public Range<Integer> parseFieldValue(String value) throws ImperatException {
        return numericField.parseFieldValue(value);
    }
}
