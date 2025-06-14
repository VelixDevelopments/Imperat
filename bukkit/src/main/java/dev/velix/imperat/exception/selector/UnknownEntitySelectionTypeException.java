package dev.velix.imperat.exception.selector;

import dev.velix.imperat.exception.ParseException;

public class UnknownEntitySelectionTypeException extends ParseException {

    public UnknownEntitySelectionTypeException(String input) {
        super(input);
    }
}
