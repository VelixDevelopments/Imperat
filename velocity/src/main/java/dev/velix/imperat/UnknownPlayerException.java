package dev.velix.imperat;

import dev.velix.imperat.exception.ImperatException;

public class UnknownPlayerException extends ImperatException {

    private final String name;

    public UnknownPlayerException(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
