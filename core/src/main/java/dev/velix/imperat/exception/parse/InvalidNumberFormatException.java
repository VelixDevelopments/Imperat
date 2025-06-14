package dev.velix.imperat.exception.parse;

import dev.velix.imperat.exception.ParseException;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.Nullable;

public class InvalidNumberFormatException extends ParseException {

    private final @Nullable NumberFormatException originalError;
    private final String numberTypeDisplay;
    private final TypeWrap<? extends Number> numericType;


    public InvalidNumberFormatException(String input, @Nullable NumberFormatException originalError, String numberTypeDisplay,
            TypeWrap<? extends Number> numericType) {
        super(input);
        this.originalError = originalError;
        this.numberTypeDisplay = numberTypeDisplay;
        this.numericType = numericType;
    }

    public String getNumberTypeDisplay() {
        return numberTypeDisplay;
    }

    public TypeWrap<? extends Number> getNumericType() {
        return numericType;
    }

    public @Nullable NumberFormatException getOriginalError() {
        return originalError;
    }
}
