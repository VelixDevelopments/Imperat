package dev.velix.imperat.util.jeflect;

import org.objectweb.asm.Type;

/**
 * The class representing the field. Contains information about the name, type, and default primitive value.
 */
public abstract class ByteField extends AbstractMember {
    private final String descriptor;
    private final LazyType type;
    private final Object value;

    protected ByteField(ByteClass parent, String descriptor, Object value, String name, int modifiers) {
        super(parent, name, modifiers);
        this.descriptor = descriptor;
        this.type = new LazyType(Type.getType(descriptor).getClassName());
        this.value = value;
    }

    /**
     * @return field class name
     */
    public String getTypeName() {
        return type.getTypeName();
    }

    /**
     * Loads a field class and returns an instance of {@link Class} containing information about it.
     *
     * @return {@link Class} that represents field type
     */
    public Class<?> getType() {
        return type.getType();
    }

    /**
     * @return field descriptor
     */
    public String getDescriptor() {
        return descriptor;
    }

    /**
     * Returns the initial value of the field.
     * If the field is not a string or a primitive, the method returns null.
     *
     * @return the initial value of the field
     */
    public Object getValue() {
        return value;
    }
}
