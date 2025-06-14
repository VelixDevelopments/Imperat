package dev.velix.imperat.exception.selector;

import dev.velix.imperat.exception.ParseException;

public class UnknownSelectorFieldException extends ParseException {

    private final String fieldEntered;

    public UnknownSelectorFieldException(String fieldEntered, String input) {
        super(input);
        this.fieldEntered = fieldEntered;
    }

    public String getFieldEntered() {
        return fieldEntered;
    }
}
