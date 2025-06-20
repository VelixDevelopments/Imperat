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

}
