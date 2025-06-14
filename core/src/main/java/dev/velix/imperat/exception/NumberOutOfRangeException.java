package dev.velix.imperat.exception;

import dev.velix.imperat.command.parameters.NumericParameter;
import dev.velix.imperat.command.parameters.NumericRange;

public class NumberOutOfRangeException extends ParseException {

    private final NumericParameter<?> parameter;
    private final Number value;
    private final NumericRange range;

    public NumberOutOfRangeException(
            final String originalInput,
            final NumericParameter<?> parameter,
            final Number value,
            final NumericRange range
    ) {
        super(originalInput);
        this.parameter = parameter;
        this.value = value;
        this.range = range;
    }

    public Number getValue() {
        return value;
    }

    public NumericRange getRange() {
        return range;
    }

    public NumericParameter<?> getParameter() {
        return parameter;
    }

}
