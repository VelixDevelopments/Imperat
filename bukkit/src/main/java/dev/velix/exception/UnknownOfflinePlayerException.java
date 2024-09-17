package dev.velix.exception;

import lombok.Getter;

@Getter
public class UnknownOfflinePlayerException extends ImperatException {

    private final String name;

    public UnknownOfflinePlayerException(final String name) {
        this.name = name;
    }
    
}
