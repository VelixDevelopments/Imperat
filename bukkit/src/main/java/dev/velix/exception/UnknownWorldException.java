package dev.velix.exception;

import lombok.Getter;

@Getter
public class UnknownWorldException extends ImperatException {

    private final String name;

    public UnknownWorldException(final String name) {
        this.name = name;
    }
    
}
