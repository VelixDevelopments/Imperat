package dev.velix.imperat.util.jeflect.fields;

/**
 * <p>A class representing a universal interface for accessing a field.
 * Depending on the type of field (static or virtual), the corresponding methods will be implemented.</p>
 * <p>Calling non-implemented will throw {@link UnsupportedOperationException}.</p>
 * <p>If the field is virtual, you can interact with it using
 * {@link FieldAccessor#get(Object)}, {@link FieldAccessor#set(Object, Object)}.</p>
 * <p>If the field is static - {@link FieldAccessor#get()}, {@link FieldAccessor#set(Object)}.</p>
 */
public interface FieldAccessor {

    /**
     * Gets the value of the virtual field. The primitives will be packed.
     *
     * @param object instance of the class declaring the field
     * @return field value
     * @throws UnsupportedOperationException if packed field is not virtual
     */
    default Object get(Object object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the value of the static field. The primitives will be packed.
     *
     * @return field value
     * @throws UnsupportedOperationException if packed field is not static
     */
    default Object get() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the value of the virtual field. The primitives will be unpacked.
     * Maybe unimplemented if the target field is final.
     *
     * @param object instance of the class declaring the field
     * @param value  the value to be assigned
     * @throws UnsupportedOperationException if packed field is not virtual
     */
    default void set(Object object, Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the value of the static field. The primitives will be unpacked.
     * Maybe unimplemented if the target field is final.
     *
     * @param value the value to be assigned
     * @throws UnsupportedOperationException if packed field is not static
     */
    default void set(Object value) {
        throw new UnsupportedOperationException();
    }
}
