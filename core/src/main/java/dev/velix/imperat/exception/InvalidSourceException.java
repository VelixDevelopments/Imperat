package dev.velix.imperat.exception;

import dev.velix.imperat.util.TypeWrap;
import lombok.Getter;

@Getter
public class InvalidSourceException extends ImperatException {
    
    private final TypeWrap<?> targetType;
    
    public InvalidSourceException(TypeWrap<?> targetType) {
        super();
        this.targetType = targetType;
    }
    
}
