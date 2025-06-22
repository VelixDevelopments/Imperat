package dev.velix.imperat.util;

import java.lang.reflect.*;
import java.util.Objects;

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
     * Extracts a generic type argument from a specific superclass in the hierarchy.
     *
     * @param targetSuperclass The exact superclass whose type argument to extract.
     * @param index The index of the type parameter.
     * @return The resolved {@link Type}, possibly a Class, ParameterizedType, or resolved array.
     */
    protected Type extractType(Class<?> targetSuperclass, int index) {
        Class<?> currentClass = getClass();

        while (currentClass != null && currentClass != Object.class) {
            Type genericSuperclass = currentClass.getGenericSuperclass();

            if (genericSuperclass instanceof ParameterizedType parameterized) {
                Class<?> rawType = (Class<?>) parameterized.getRawType();

                if (rawType.equals(targetSuperclass)) {
                    Type[] actualArgs = parameterized.getActualTypeArguments();

                    if (index < 0 || index >= actualArgs.length) {
                        throw new IndexOutOfBoundsException("No type argument at index " + index);
                    }

                    Type extracted = actualArgs[index];

                    // If it's a type variable like E
                    if (extracted instanceof TypeVariable<?> variable) {
                        return resolveTypeVariable(variable, getClass());
                    }

                    // If it's something like E[]
                    if (extracted instanceof GenericArrayType arrayType) {
                        Type component = arrayType.getGenericComponentType();
                        if (component instanceof TypeVariable<?> variable) {
                            Type resolved = resolveTypeVariable(variable, getClass());
                            if (resolved instanceof Class<?> clazz) {
                                return Array.newInstance(clazz, 0).getClass(); // e.g. String[]
                            }
                        }
                    }

                    return extracted;
                }

                currentClass = rawType;

            } else if (genericSuperclass instanceof Class<?> raw) {
                currentClass = raw;

            } else {
                break;
            }
        }

        throw new IllegalStateException("Superclass " + targetSuperclass.getName() +
            " not found in hierarchy of " + getClass().getName());
    }

    /**
     * Resolves a {@link TypeVariable} (like T or E) to a concrete {@link Type}, using the hierarchy starting at the given class.
     */
    private Type resolveTypeVariable(TypeVariable<?> variable, Class<?> startClass) {
        Class<?> currentClass = startClass;

        while (currentClass != null && currentClass != Object.class) {
            Type genericSuperclass = currentClass.getGenericSuperclass();

            if (genericSuperclass instanceof ParameterizedType parameterized) {
                Class<?> raw = (Class<?>) parameterized.getRawType();

                if (Objects.equals(raw, variable.getGenericDeclaration())) {
                    TypeVariable<?>[] vars = raw.getTypeParameters();
                    Type[] args = parameterized.getActualTypeArguments();

                    for (int i = 0; i < vars.length; i++) {
                        if (vars[i].getName().equals(variable.getName())) {
                            return args[i];
                        }
                    }
                }

                currentClass = raw;
            } else if (genericSuperclass instanceof Class<?> raw) {
                currentClass = raw;
            } else {
                break;
            }
        }

        return null;
    }

}
