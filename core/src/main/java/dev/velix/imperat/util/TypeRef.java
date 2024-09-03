package dev.velix.imperat.util;

import com.google.common.reflect.TypeToken;

public final class TypeRef<T> extends TypeToken<T> {
	
	
	public TypeRef() {
		super(TypeRef.class);
	}
	
}
