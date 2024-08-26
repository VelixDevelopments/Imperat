package dev.velix.imperat.command.parameters;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a behaviour that deals with numeric
 * inputs if they are ranged from min to max using {@link NumericRange}
 */
public interface NumericParameter extends CommandParameter {

    /**
     * @return The actual range of the numeric parameter
     * returns null if no range is specified !
     */
    @Nullable
    NumericRange getRange();

    default boolean hasRange() {
        return getRange() != null;
    }

    default boolean matchesRange(double value) {
        var range = getRange();
        return range != null &&
                value >= range.getMin() && value <= range.getMax();
    }

}
