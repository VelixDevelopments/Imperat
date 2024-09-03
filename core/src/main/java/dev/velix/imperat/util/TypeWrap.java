package dev.velix.imperat.util;

import lombok.Getter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class TypeWrap<T> {
	
	@Getter
	private final Type type;
	private final Class<?> rawType;
	
	public TypeWrap() {
		this.type = extractType();
		this.rawType = extractRawType(type);
	}
	
	private Type extractType() {
		Type superclass = getClass().getGenericSuperclass();
		if (superclass instanceof ParameterizedType parameterizedType) {
			return parameterizedType.getActualTypeArguments()[0];
		} else {
			throw new IllegalArgumentException("TypeWrap must be created with a parameterized type.");
		}
	}
	
	private Class<?> extractRawType(Type type) {
		if (type instanceof Class<?> cls) {
			return cls;
		} else if (type instanceof ParameterizedType parameterizedType) {
			return (Class<?>) parameterizedType.getRawType();
		} else {
			throw new IllegalArgumentException("Unsupported type: " + type);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Class<? super T> getRawType() {
		return (Class<? super T>) rawType;
	}
	
}
