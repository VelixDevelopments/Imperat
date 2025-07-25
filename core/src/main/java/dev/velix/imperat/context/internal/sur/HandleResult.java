package dev.velix.imperat.context.internal.sur;

import dev.velix.imperat.exception.ImperatException;
import org.jetbrains.annotations.Nullable;

public enum HandleResult {
    TERMINATE,
    GO_BACK, 
    CONTINUE,
    FAILURE;
    
    private ImperatException exception;
    
    public static HandleResult failure(ImperatException exception) {
        HandleResult result = FAILURE;
        result.exception = exception;
        return result;
    }
    
    @Nullable
    public ImperatException getException() {
        return exception;
    }
}