package dev.velix.imperat.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public abstract class TypeCapturer {

    /**
     * Extracts a generic type argument from this class's direct superclass.
     *
     * @param index The index of the type argument (e.g. 0 for the first, 1 for the second).
     * @return The resolved {@link Type}.
     */
    protected Type extractType(int index) {
        Type genericSuperClass = this.getClass().getGenericSuperclass();
        if (genericSuperClass instanceof ParameterizedType parameterized) {
            Type[] args = parameterized.getActualTypeArguments();
            if (index < 0 || index >= args.length) {
                throw new IndexOutOfBoundsException("No type argument at index " + index);
            }

            Type targetType = args[index];

            // Handle TypeVariable (like E[])
            if (targetType instanceof TypeVariable) {
                throw new IllegalStateException("Cannot resolve generic type variable: " + targetType +
                        ". Consider passing the concrete type explicitly to the constructor.");
            }

            return targetType;
        }

        throw new IllegalStateException("Superclass is not parameterized: " + genericSuperClass);
    }

    /**
     * Attempts to cast the type argument to a Class object.
     */
    @SuppressWarnings("unchecked")
    protected <T> Class<T> extractTypeClass(int index) {
        Type type = extractType(index);

        if (type instanceof Class<?> cls) {
            return (Class<T>) cls;
        }

        if (type instanceof ParameterizedType pt && pt.getRawType() instanceof Class<?> raw) {
            return (Class<T>) raw;
        }

        throw new IllegalStateException("Type at index " + index + " is not a Class: " + type);
    }
}
