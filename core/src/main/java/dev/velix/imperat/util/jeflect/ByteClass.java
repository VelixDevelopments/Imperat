package dev.velix.imperat.util.jeflect;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class representing a class or interface loaded from bytecode.
 */
public abstract class ByteClass extends AbstractMember {
    private final List<LazyType> interfaces;
    private final LazyType type;
    private final LazyType superType;

    protected ByteClass(String superType,
                        String[] interfaces,
                        String name,
                        int modifiers) {
        super(null, name, modifiers);
        this.type = new LazyType(name);
        this.superType = new LazyType(superType);
        if (interfaces == null) {
            this.interfaces = Collections.emptyList();
        } else {
            this.interfaces = Arrays
                    .stream(interfaces)
                    .map(LazyType::new)
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    /**
     * Returns fields that are declared by this class.
     * If this class has no fields, the return value is a list of length 0.
     * The returned {@link List} is will always be instanced of UnmodifiableList.
     *
     * @return fields that are declared by this class
     */
    public abstract List<ByteField> getFields();

    /**
     * Returns methods that are declared by this class.
     * If this class has no methods, the return value is a list of length 0.
     * The returned {@link List} is will always be instanced of UnmodifiableList.
     *
     * @return methods that are declared by this class
     */
    public abstract List<ByteMethod> getMethods();

    /**
     * Searches for the method contained in this class.
     *
     * @param name       method name
     * @param descriptor method descriptor
     * @return found method instance or null
     */
    public ByteMethod getMethod(String name, String descriptor) {
        var methods = getMethods();
        for (var method : methods) {
            if (method.getName().equals(name) && method.getDescriptor().equals(descriptor)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Searches for the field contained in this class.
     *
     * @param name       field name
     * @param descriptor field descriptor
     * @return found field instance or null
     */
    public ByteField getField(String name, String descriptor) {
        var fields = getFields();
        for (var field : fields) {
            if (field.getName().equals(name) && field.getDescriptor().equals(descriptor)) {
                return field;
            }
        }
        return null;
    }

    /**
     * @return {@link LazyType} instance containing super class of this class
     */
    public LazyType getSuperclass() {
        return superType;
    }

    /**
     * @return {@link LazyType} instance containing this class
     */
    public LazyType getType() {
        return type;
    }

    /**
     * Returns interfaces that are implemented by this class.
     * If this class does not implement interfaces, the return value is a list of length 0.
     * The returned {@link List} is will always be instanced of UnmodifiableList.
     *
     * @return interfaces that are implemented by this class
     */
    public List<LazyType> getInterfaces() {
        return interfaces;
    }
}
