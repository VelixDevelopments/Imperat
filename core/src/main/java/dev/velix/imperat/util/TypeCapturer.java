package dev.velix.imperat.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeCapturer {

    /**
     * Extracts a generic type argument from this class's direct superclass.
     *
     * @param index The index of the type argument (e.g. 0 for the first, 1 for the second).
     * @return The resolved {@link Type}.
     */
    protected Type extractType(int index) {
        Type genericSuperclass = getClass().getGenericSuperclass();

        if (genericSuperclass instanceof ParameterizedType parameterized) {
            Type[] args = parameterized.getActualTypeArguments();
            if (index < 0 || index >= args.length) {
                throw new IndexOutOfBoundsException("No type argument at index " + index);
            }
            return args[index];
        }

        throw new IllegalStateException("Superclass is not parameterized: " + genericSuperclass);
    }

    /**
     * Extracts a generic type argument from a specific superclass in this class's hierarchy.
     *
     * @param targetSuperclass The exact class whose generic argument should be extracted.
     * @param index The index of the type argument (e.g. 0 for the first).
     * @return The resolved {@link Type}.
     */
    protected Type extractType(Class<?> targetSuperclass, int index) {
        Class<?> currentClass = getClass();

        while (currentClass != null && currentClass != Object.class) {
            Type genericSuperclass = currentClass.getGenericSuperclass();

            if (genericSuperclass instanceof ParameterizedType parameterized) {
                Class<?> raw = (Class<?>) parameterized.getRawType();
                if (raw.equals(targetSuperclass)) {
                    Type[] args = parameterized.getActualTypeArguments();
                    if (index < 0 || index >= args.length) {
                        throw new IndexOutOfBoundsException("No type argument at index " + index);
                    }
                    return args[index];
                }
                currentClass = raw;
            } else if (genericSuperclass instanceof Class<?> raw) {
                currentClass = raw;
            } else {
                break;
            }
        }

        throw new IllegalStateException("Superclass " + targetSuperclass.getName() + " not found in hierarchy of " + getClass().getName());
    }
}
