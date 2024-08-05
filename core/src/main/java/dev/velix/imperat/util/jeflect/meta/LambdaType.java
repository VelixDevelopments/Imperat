package dev.velix.imperat.util.jeflect.meta;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

/**
 * Container for lambda classes.
 *
 * @param <T> the type corresponding to the lambda type
 */
public final class LambdaType<T> {
    private final Class<T> lambdaClass;
    private final Method lambdaMethod;

    private LambdaType(Class<T> clazz, Method method) {
        this.lambdaClass = clazz;
        this.lambdaMethod = method;
    }

    /**
     * Checks and packages the passed class.
     *
     * @param clazz clazz to be packaged
     * @param <T>   the type corresponding to the lambda type
     * @return instance of {@link LambdaType}
     */
    public static <T> LambdaType<T> fromClass(Class<T> clazz) {
        Objects.requireNonNull(clazz);
        if (!clazz.isInterface()) {
            throw new IllegalArgumentException("Invalid lambda class");
        }
        var found = Arrays.
                stream(clazz.getMethods()).
                filter(e -> Modifier.isAbstract(e.getModifiers())).
                findFirst();
        if (found.isEmpty()) {
            throw new IllegalArgumentException("Invalid lambda class");
        }
        return new LambdaType<>(clazz, found.get());
    }

    /**
     * @return java class object, contains lambda type
     */
    public Class<T> getLambdaClass() {
        return lambdaClass;
    }

    /**
     * @return java method object, contains lambda method
     */
    public Method getLambdaMethod() {
        return lambdaMethod;
    }

    @Override
    public int hashCode() {
        return lambdaClass.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LambdaType)) {
            return false;
        }
        return lambdaClass.equals(((LambdaType<?>) obj).lambdaClass);
    }

    @Override
    public String toString() {
        return "Lambda " + lambdaClass.getSimpleName() + " with method " + lambdaMethod.getName();
    }
}
