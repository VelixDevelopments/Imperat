package dev.velix.imperat.exception;

import org.jetbrains.annotations.Nullable;

public class InvalidLocationFormatException extends ParseException {

    private final Reason reason;
    private final @Nullable String inputX, inputY, inputZ;

    public InvalidLocationFormatException(String input, Reason reason, @Nullable String inputX, @Nullable String inputY, @Nullable String inputZ) {
        super(input);
        this.reason = reason;
        this.inputX = inputX;
        this.inputY = inputY;
        this.inputZ = inputZ;
    }

    public InvalidLocationFormatException(String input, Reason reason) {
        this(input, reason, null, null, null);
    }

    public Reason getReason() {
        return reason;
    }

    public @Nullable String getInputX() {
        return inputX;
    }

    public @Nullable String getInputY() {
        return inputY;
    }

    public @Nullable String getInputZ() {
        return inputZ;
    }

    public enum Reason {

        INVALID_X_COORD,

        INVALID_Y_COORD,

        INVALID_Z_COORD,

        NO_WORLDS_AVAILABLE,

        WRONG_FORMAT;

    }

}
