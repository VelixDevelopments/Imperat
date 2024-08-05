package dev.velix.imperat.util.jeflect;


import java.util.Map;

/**
 * A class representing an annotation instance.
 */
public final class ByteAnnotation {
    private final LazyType type;
    private final Map<String, Object> fields;

    public ByteAnnotation(String className, Map<String, Object> fields) {
        this.type = new LazyType(className);
        this.fields = fields;
    }

    /**
     * Returns the annotation class name.
     *
     * @return the annotation class name
     */
    public String getAnnotationClassName() {
        return type.getTypeName();
    }

    /**
     * Loads and returns the annotation class.
     *
     * @return the annotation class
     */
    public Class<?> getAnnotationClass() {
        return type.getType();
    }

    /**
     * Searches for the annotation field by its name.
     *
     * @param name field name
     * @param <T>  the type to which the field value will be cast
     * @return field value or null if there is no such field
     */
    @SuppressWarnings("unchecked")
    public <T> T getField(String name) {
        return (T) fields.get(name);
    }
}
