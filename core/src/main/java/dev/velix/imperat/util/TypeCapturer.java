package dev.velix.imperat.util;

import java.lang.reflect.*;
import java.util.Objects;

public abstract class TypeCapturer {

    /**
     * Extracts a generic type argument from this class's direct superclass.
     *
     * @param index The index of the type argument (e.g. 0 for the first).
     * @return The resolved {@link Type}.
     */
    protected Type extractType(int index) {
        Type genericSuperClass = this.getClass().getGenericSuperclass();

        if (genericSuperClass instanceof ParameterizedType parameterized) {
            Type[] args = parameterized.getActualTypeArguments();
            if (index < 0 || index >= args.length) {
                throw new IndexOutOfBoundsException("No type argument at index " + index);
            }

            Type extracted = args[index];
            return resolveVariableType(extracted, this.getClass());
        }

        throw new IllegalStateException("Superclass is not parameterized: " + genericSuperClass);
    }

    /**
     * Extracts a generic type argument from a specific superclass in the hierarchy.
     *
     * @param targetSuperclass The exact superclass whose type argument to extract.
     * @param index The index of the type parameter.
     * @return The resolved {@link Type}, possibly a Class, ParameterizedType, or array.
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
                    return resolveVariableType(extracted, getClass());
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
     * Resolves a {@link Type} (including {@link TypeVariable}, {@link GenericArrayType}, {@link ParameterizedType})
     * to a concrete {@link Type} using the type hierarchy of the given context class.
     */
    private Type resolveVariableType(Type type, Class<?> contextClass) {
        if (type instanceof Class<?>) {
            return type; // Already resolved
        }

        if (type instanceof ParameterizedType pt) {
            Type[] resolvedArgs = new Type[pt.getActualTypeArguments().length];
            for (int i = 0; i < resolvedArgs.length; i++) {
                resolvedArgs[i] = resolveVariableType(pt.getActualTypeArguments()[i], contextClass);
            }
            return new ResolvedParameterizedType((Class<?>) pt.getRawType(), resolvedArgs, pt.getOwnerType());
        }

        if (type instanceof GenericArrayType gat) {
            Type component = resolveVariableType(gat.getGenericComponentType(), contextClass);
            if (component instanceof Class<?> cls) {
                return Array.newInstance(cls, 0).getClass(); // Convert to array class like String[].class
            }
            return new ResolvedGenericArrayType(component);
        }

        if (type instanceof TypeVariable<?> variable) {
            Class<?> currentClass = contextClass;

            while (currentClass != null && currentClass != Object.class) {
                Type genericSuperclass = currentClass.getGenericSuperclass();

                if (genericSuperclass instanceof ParameterizedType parameterized) {
                    Class<?> raw = (Class<?>) parameterized.getRawType();
                    TypeVariable<?>[] vars = raw.getTypeParameters();
                    Type[] args = parameterized.getActualTypeArguments();

                    for (int i = 0; i < vars.length; i++) {
                        if (vars[i].getName().equals(variable.getName())) {
                            return resolveVariableType(args[i], raw);
                        }
                    }

                    currentClass = raw;
                } else if (genericSuperclass instanceof Class<?> raw) {
                    currentClass = raw;
                } else {
                    break;
                }
            }

            return variable; // Still unresolved
        }

        return type; // Fallback
    }

    /**
     * Implementation of ParameterizedType with fully resolved arguments.
     */
    private static class ResolvedParameterizedType implements ParameterizedType {
        private final Class<?> raw;
        private final Type[] args;
        private final Type owner;

        ResolvedParameterizedType(Class<?> raw, Type[] args, Type owner) {
            this.raw = raw;
            this.args = args;
            this.owner = owner;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return args;
        }

        @Override
        public Type getRawType() {
            return raw;
        }

        @Override
        public Type getOwnerType() {
            return owner;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(raw.getTypeName());
            if (args.length > 0) {
                sb.append("<");
                for (int i = 0; i < args.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(args[i].getTypeName());
                }
                sb.append(">");
            }
            return sb.toString();
        }
    }

    /**
     * Implementation of GenericArrayType with fully resolved component type.
     */
    private static class ResolvedGenericArrayType implements GenericArrayType {
        private final Type component;

        ResolvedGenericArrayType(Type component) {
            this.component = component;
        }

        @Override
        public Type getGenericComponentType() {
            return component;
        }

        @Override
        public String toString() {
            return component.getTypeName() + "[]";
        }
    }
}
