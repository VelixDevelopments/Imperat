package dev.velix.imperat.exception;

import lombok.Getter;

@Getter
public class ExecutionError extends ImperatException {

    private final String message;
    private final ErrorLevel type;
    
    public ExecutionError(
            final String msg,
            final Object... args
    ) {
        this.type = ErrorLevel.SEVERE;
        this.message = String.format(msg, args);
    }
    
    public ExecutionError(
            final ErrorLevel type,
            final String msg,
            final Object... args
    ) {
        this.type = type;
        this.message = String.format(msg, args);
    }

    @Override
    public String getMessage() {
        return this.message;
    }
    
    public enum ErrorLevel {
        REPLY,
        WARN,
        SEVERE
    }

}
