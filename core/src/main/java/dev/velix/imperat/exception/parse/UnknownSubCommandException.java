package dev.velix.imperat.exception.parse;

import dev.velix.imperat.exception.ParseException;

public class UnknownSubCommandException extends ParseException {

    public UnknownSubCommandException(String input) {
        super(input);
    }
}
