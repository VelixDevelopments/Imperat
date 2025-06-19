package dev.velix.imperat.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

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
            Type extracted = args[index];
            debugType(extracted);
            return extracted;
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
                    Type extracted = args[index];
                    debugType(extracted);
                    return extracted;
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

    /**
     * Debug helper to print details about a Type using ImperatDebugger.warning,
     * including the original class name from which the type is extracted.
     */
    protected void debugType(Type type) {
        String className = getClass().getName();
        if (type instanceof Class<?>) {
            ImperatDebugger.warning(className + " - Type is Class: " + ((Class<?>) type).getName());
        } else if (type instanceof ParameterizedType) {
            ImperatDebugger.warning(className + " - Type is ParameterizedType: " + type);
        } else if (type instanceof GenericArrayType) {
            ImperatDebugger.warning(className + " - Type is GenericArrayType: " + type);
        } else if (type instanceof TypeVariable<?>) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            ImperatDebugger.warning(className + " - Type is TypeVariable: " + tv.getName() + ", bounds: " + java.util.Arrays.toString(tv.getBounds()));
        } else if (type instanceof WildcardType) {
            ImperatDebugger.warning(className + " - Type is WildcardType: " + type);
        } else {
            ImperatDebugger.warning(className + " - Type is unknown: " + type);
        }
    }
}
