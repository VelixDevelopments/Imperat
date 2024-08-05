package dev.velix.imperat.util.jeflect;

import com.github.romanqed.jfunc.Exceptions;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A set of utilities for interacting with reflection.
 */
public final class JeflectUtil {
    private JeflectUtil() {
    }

    /**
     * Extracts the value from the annotation by name using basic reflection tools.
     *
     * @param annotation annotation object to be extracted from
     * @param value      name of the extracted value
     * @param <T>        type of extracted value
     * @return extracted value, or null in case of an error
     * @throws RuntimeException if a value with the specified name was not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T extractAnnotationValue(Annotation annotation, String value) {
        var annotationType = annotation.annotationType();
        var found = Exceptions.suppress(() -> annotationType.getDeclaredMethod(value));
        found.setAccessible(true);
        try {
            return (T) found.invoke(annotation);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the value from the annotation by name "value" using basic reflection tools.
     *
     * @param annotation annotation object to be extracted from
     * @param <T>        type of extracted value
     * @return extracted value, or null in case of an error
     */
    public static <T> T extractAnnotationValue(Annotation annotation) {
        return extractAnnotationValue(annotation, "value");
    }

    /**
     * Gets all enum values as array.
     *
     * @param clazz enum class
     * @param <T>   enum type
     * @return array contains enum values
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T[] getEnumValues(Class<T> clazz) {
        var method = Exceptions.suppress(() -> clazz.getDeclaredMethod("values"));
        method.setAccessible(true);
        return (T[]) Exceptions.suppress(() -> method.invoke(null));
    }

    /**
     * Gets all enum values as stream.
     *
     * @param clazz enum class
     * @param <T>   enum type
     * @return stream contains enum values
     */
    public static <T extends Enum<T>> Stream<T> getEnumStream(Class<T> clazz) {
        return Arrays.stream(getEnumValues(clazz));
    }

    /**
     * Builds map from enum values.
     *
     * @param clazz    enum class
     * @param function map function
     * @param <T>      enum type
     * @param <V>      key type
     * @return map contains value-enum pairs
     */
    public static <T extends Enum<T>, V> Map<V, T> enumToMap(Class<T> clazz, Function<T, V> function) {
        var values = getEnumValues(clazz);
        var ret = new HashMap<V, T>();
        for (var value : values) {
            ret.put(function.apply(value), value);
        }
        return ret;
    }
}
