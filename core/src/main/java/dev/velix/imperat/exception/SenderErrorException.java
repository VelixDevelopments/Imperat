package dev.velix.imperat.exception;

public class SenderErrorException extends ImperatException {
    
    private final String message;
    
    public SenderErrorException(String msg, Object... args) {
        this.message = String.format(msg, args);
    }
    
    @Override
    public String getMessage() {
        return this.message;
    }
    
}
