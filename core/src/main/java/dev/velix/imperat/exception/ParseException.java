package dev.velix.imperat.exception;

public abstract class ParseException extends ImperatException {

    protected final String input;

    public ParseException(String input) {
        this.input = input;
    }

    public String getInput() {
        return input;
    }
}
