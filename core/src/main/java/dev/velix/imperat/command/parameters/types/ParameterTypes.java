package dev.velix.imperat.command.parameters.types;

import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ParameterTypes {
    
    private final static Map<Type, ParameterType> PARAMETER_TYPES = new LinkedHashMap<>((int) Math.ceil(6 / 0.75));
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
    
    private ParameterTypes() {
        throw new AssertionError();
    }
    
    public static ParameterType getParamType(Type type) {
        TypeWrap<?> token = TypeWrap.of(type).wrap();
        
        if (token.isArray()) {
            return PARAMETER_TYPES.getOrDefault(Objects.requireNonNull(token.getComponentType()).getType(), STRING_TYPE);
        } else if (token.isSubtypeOf(Collection.class)) {
            return PARAMETER_TYPES.getOrDefault(token.getRawType(), STRING_TYPE);
        } else {
            return PARAMETER_TYPES.getOrDefault(type, STRING_TYPE);
        }
        
    }
    
}
