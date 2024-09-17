package dev.velix.exception;

import dev.velix.command.parameters.NumericParameter;
import dev.velix.command.parameters.NumericRange;

public class NumberOutOfRangeException extends SourceException {
    
    public NumberOutOfRangeException(
            final NumericParameter parameter,
            final Number value,
            final NumericRange range
    ) {
        super("Value '" + value + "' entered for parameter '" + parameter.format() + "' must be " + formatRange(range));
    }
    
    private static String formatRange(final NumericRange range) {
        final StringBuilder builder = new StringBuilder();
        if (range.getMin() != Double.MIN_VALUE && range.getMax() != Double.MAX_VALUE)
            builder.append("within ").append(range.getMin()).append('-').append(range.getMax());
        else if (range.getMin() != Double.MIN_VALUE)
            builder.append("at least '").append(range.getMin()).append("'");
        else if (range.getMax() != Double.MAX_VALUE)
            builder.append("at most '").append(range.getMax()).append("'");
        else
            builder.append("(Open range)");
        
        return builder.toString();
    }
    
}
