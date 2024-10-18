package dev.velix.imperat.selector.field;

import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;

/**
 * RangedNumericField is a concrete implementation of AbstractField that handles numeric fields with range values.
 * The range is represented using the Range class, which allows specifying minimum and/or maximum bounds.
 *
 * @param <N> The type of the numeric value that this field handles, which extends Number.
 */
public final class RangedNumericField<N extends Number> extends AbstractField<Range<N>> {

    private final static String RANGE_CHARACTER = "..";

    private final NumericField<N> numericField;

    private RangedNumericField(NumericField<N> numericField) {
        super(numericField.name, new TypeWrap<>() {
        });
        this.numericField = numericField;
    }


    public static <N extends Number> RangedNumericField<N> of(NumericField<N> numericField) {
        return new RangedNumericField<>(numericField);
    }

    /**
     * Parses the given string representation of the value and converts it into the field's value type.
     *
     * @param value the string representation of the value to be parsed
     * @return the parsed value of the field's type
     * @throws ImperatException if the parsing fails
     */
    @Override
    public Range<N> parseFieldValue(String value) throws ImperatException {
        try {
            N numericValue = numericField.parseNumber(value);
            return Range.atLeast(numericValue);
        } catch (ImperatException ex) {
            N rangeValue = numericField.parseNumber(value.replace(RANGE_CHARACTER, ""));

            if (value.startsWith(RANGE_CHARACTER)) {
                //less than or equal to value
                return Range.atLeast(rangeValue);
            } else if (value.endsWith(RANGE_CHARACTER)) {
                return Range.atMost(rangeValue);
            } else if (value.contains(RANGE_CHARACTER)) {
                String[] minMaxSplit = value.split(RANGE_CHARACTER);
                if (minMaxSplit.length > 2) {
                    throw new SourceException("Invalid distance range format '%s'", value);
                }
                String minStr = minMaxSplit[0], maxStr = minMaxSplit[1];
                if (!TypeUtility.isNumber(minStr)) {
                    throw new SourceException("Invalid min-value '%s'", minStr);
                }

                if (!TypeUtility.isNumber(maxStr)) {
                    throw new SourceException("Invalid max-value '%s'", maxStr);
                }

                N min = numericField.parseFieldValue(minStr), max = numericField.parseFieldValue(maxStr);
                return Range.of(min, max);
            }
            throw ex;
        }
    }

}
