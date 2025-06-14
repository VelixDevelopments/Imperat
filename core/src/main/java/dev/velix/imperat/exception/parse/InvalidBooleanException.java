package dev.velix.imperat.exception.parse;

import dev.velix.imperat.exception.ParseException;

public final class InvalidBooleanException extends ParseException {

    public InvalidBooleanException(String input) {
        super(input);
    }
}
