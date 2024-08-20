package dev.velix.imperat.exceptions.context;

import dev.velix.imperat.command.parameters.NumericParameter;
import dev.velix.imperat.command.parameters.NumericRange;

public class NumberOutOfRangeException extends ContextResolveException {

    public NumberOutOfRangeException(
            final NumericParameter parameter,
            final double value,
            final NumericRange range
    ) {
        super("Value '" + value + "' entered for parameter '"
                + parameter.getName() + "' must be " + formatRange(range));
    }

    private static String formatRange(NumericRange range) {
        StringBuilder builder = new StringBuilder();
        if (range.min() != Double.MIN_VALUE && range.max() != Double.MAX_VALUE)
            builder.append("within").append(range.min()).append('-').append(range.max());
        else if (range.min() != Double.MIN_VALUE)
            builder.append("at least '").append(range.min()).append("'");
        else if (range.max() != Double.MAX_VALUE)
            builder.append("at most '").append(range.max()).append("'");
        else
            builder.append("(Open range)");

        return builder.toString();
    }

}
