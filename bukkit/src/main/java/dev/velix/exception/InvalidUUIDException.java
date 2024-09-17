package dev.velix.exception;

import lombok.Getter;

@Getter
public class InvalidUUIDException extends ImperatException {

    private final String raw;

    public InvalidUUIDException(final String raw) {
        this.raw = raw;
    }
    
}
