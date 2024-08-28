package dev.velix.imperat.exceptions.context;

import dev.velix.imperat.command.parameters.NumericParameter;
import dev.velix.imperat.command.parameters.NumericRange;

public class NumberOutOfRangeException extends ContextResolveException {

    public NumberOutOfRangeException(
            final NumericParameter parameter,
            final Number value,
            final NumericRange range
    ) {
        super("Value '" + value + "' entered for parameter '"
                + parameter.getName() + "' must be " + formatRange(range));
    }

    private static String formatRange(NumericRange range) {
        StringBuilder builder = new StringBuilder();
        if (range.getMin() != Double.MIN_VALUE && range.getMax() != Double.MAX_VALUE)
            builder.append("within").append(range.getMin()).append('-').append(range.getMax());
        else if (range.getMin() != Double.MIN_VALUE)
            builder.append("at least '").append(range.getMax()).append("'");
        else if (range.getMax() != Double.MAX_VALUE)
            builder.append("at most '").append(range.getMax()).append("'");
        else
            builder.append("(Open range)");

        return builder.toString();
    }

}
