package dev.velix.exception;

import lombok.Getter;

@Getter
public class UnknownPlayerException extends ImperatException {

    private final String name;

    public UnknownPlayerException(final String name) {
        this.name = name;
    }
    
}
