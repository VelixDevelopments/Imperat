package dev.velix.imperat.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

public final class TypeUtility {

    static Set<Class<?>> NUMERIC_PRIMITIVES = Set.of(
            short.class, byte.class, int.class,
            long.class, float.class, double.class
    );

    static Map<Class<?>, Class<?>> PRIMITIVES_TO_BOXED = Map.of(
            boolean.class, Boolean.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            byte.class, Byte.class
    );

    private TypeUtility() {
    }

    public static boolean isInteger(String string) {
        if (string == null) return false;
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static boolean isBoolean(String string) {
        if (string == null) return false;
        return Boolean.parseBoolean(string);
    }

    public static boolean isDouble(String str) {
        if (str == null) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isLong(String str) {
        if (str == null) return false;
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNumericType(Class<?> type) {
        return Number.class.isAssignableFrom(type) || (NUMERIC_PRIMITIVES.contains(type));
    }

    public static boolean isPrimitive(Class<?> type) {
        return PRIMITIVES_TO_BOXED.get(type) != null;
    }

    public static @NotNull Class<?> primitiveToBoxed(Class<?> primitive) {
        return PRIMITIVES_TO_BOXED.getOrDefault(primitive, primitive);
    }

    public static <T> boolean matches(@NotNull Class<T> type1, @NotNull Class<?> type2) {
        var t1 = isPrimitive(type1) ? primitiveToBoxed(type1) : type1;
        var t2 = isPrimitive(type2) ? primitiveToBoxed(type2) : type2;
        return t1.getName().equals(t2.getName());
    }

    public static Type getInsideGeneric(Type genericType, Type fallback) {
        try {
            return ((ParameterizedType) genericType).getActualTypeArguments()[0];
        } catch (ClassCastException e) {
            return fallback;
        }
    }
}
