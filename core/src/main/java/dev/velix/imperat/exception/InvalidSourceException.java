package dev.velix.imperat.exception;

import dev.velix.imperat.util.TypeWrap;

public class InvalidSourceException extends ImperatException {
	
	private final TypeWrap<?> targetType;
	
	public InvalidSourceException(TypeWrap<?> targetType) {
		super();
		this.targetType = targetType;
	}
	
	public TypeWrap<?> getTargetType() {
		return targetType;
	}
	
}
