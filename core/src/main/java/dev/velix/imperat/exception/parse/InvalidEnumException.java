package dev.velix.imperat.exception.parse;

import dev.velix.imperat.exception.ParseException;

public class InvalidEnumException extends ParseException {

    private final Class<? extends Enum> enumType;
    public InvalidEnumException(String input, Class<? extends Enum> enumType) {
        super(input);
        this.enumType = enumType;
    }

    public Class<? extends Enum> getEnumType() {
        return enumType;
    }

}
