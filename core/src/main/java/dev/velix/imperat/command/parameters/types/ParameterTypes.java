package dev.velix.imperat.command.parameters.types;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import dev.velix.imperat.util.TypeUtility;

import java.lang.reflect.Type;
import java.util.*;

public final class ParameterTypes {
	
	private ParameterTypes() {
		throw new AssertionError();
	}
	
	private final static Map<Type, ParameterType> PARAMETER_TYPES = Maps.newLinkedHashMapWithExpectedSize(6);
	
	private final static ParameterType STRING_TYPE = new ParameterType() {
		@Override
		public Type type() {
			return String.class;
		}
		
		@Override
		public boolean matchesInput(String input) {
			return true;
		}
	};
	
	static {
		PARAMETER_TYPES.put(String.class, STRING_TYPE);
		
		PARAMETER_TYPES.put(Boolean.class, new ParameterType() {
			@Override
			public Type type() {
				return Boolean.class;
			}
			
			@Override
			public boolean matchesInput(String input) {
				return input.equalsIgnoreCase("true")
								|| input.equalsIgnoreCase("false");
			}
		});
		
		PARAMETER_TYPES.put(Integer.class, new ParameterType() {
			@Override
			public Type type() {
				return Integer.TYPE;
			}
			
			@Override
			public boolean matchesInput(String input) {
				return TypeUtility.isInteger(input);
			}
		});
		
		PARAMETER_TYPES.put(Long.class, new ParameterType() {
			@Override
			public Type type() {
				return Long.class;
			}
			
			@Override
			public boolean matchesInput(String input) {
				return TypeUtility.isLong(input);
			}
		});
		
		PARAMETER_TYPES.put(Float.TYPE, new ParameterType() {
			@Override
			public Type type() {
				return Float.class;
			}
			
			@Override
			public boolean matchesInput(String input) {
				return TypeUtility.isFloat(input);
			}
		});
		PARAMETER_TYPES.put(Double.class, new ParameterType() {
			@Override
			public Type type() {
				return Double.class;
			}
			
			@Override
			public boolean matchesInput(String input) {
				return TypeUtility.isDouble(input);
			}
		});
	}
	
	public static ParameterType getParamType(Type type) {
		TypeToken<?> token = TypeToken.of(type);
		token = token.wrap();
	
		if(token.isArray()) {
			return PARAMETER_TYPES.getOrDefault(Objects.requireNonNull(token.getComponentType()).getType(), STRING_TYPE);
		}else if(token.isSubtypeOf(Collection.class)) {
			return PARAMETER_TYPES.getOrDefault(token.getRawType(), STRING_TYPE);
		}else {
			return PARAMETER_TYPES.getOrDefault(type, STRING_TYPE);
		}
		
	}
	
}
