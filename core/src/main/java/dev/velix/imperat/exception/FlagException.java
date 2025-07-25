package dev.velix.imperat.exception;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("1.9.8")
public class FlagException extends ImperatException {
    public FlagException(String message) {
        super(message);
    }
}
