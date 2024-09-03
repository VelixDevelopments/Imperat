package dev.velix.imperat.util;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;

public final class TypeRef<T> {
	
	private final TypeToken<T> typeToken;
	
	{
		typeToken = new TypeToken<>() {};
	}
	
	public Type getType() {
		return typeToken.getType();
	}
	
	public Class<? super T> getRawType() {
		return typeToken.getRawType();
	}
	
}
