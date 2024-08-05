package dev.velix.imperat.util.jeflect;

import org.objectweb.asm.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The class representing the method. Contains information about the signature and annotations.
 */
public abstract class ByteMethod extends AbstractMember {
    private final String descriptor;
    private final LazyType returnType;
    private final List<LazyType> exceptionTypes;

    protected ByteMethod(ByteClass parent, String descriptor, String[] exceptions, String name, int modifiers) {
        super(parent, name, modifiers);
        this.descriptor = descriptor;
        this.returnType = new LazyType(Type.getType(descriptor).getReturnType().getClassName());
        if (exceptions == null) {
            this.exceptionTypes = Collections.emptyList();
        } else {
            this.exceptionTypes = Arrays
                    .stream(exceptions)
                    .map(LazyType::new)
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    /**
     * @return method descriptor
     */
    public String getDescriptor() {
        return descriptor;
    }

    /**
     * Returns a list of parameters.
     * If this method have no parameters, the return value is a list of length 0.
     * The returned {@link List} is will always be instanced of UnmodifiableList.
     *
     * @return a list of parameters
     */
    public abstract List<ByteParameter> getParameters();

    /**
     * @return method parameter count
     */
    public int getParameterCount() {
        return getParameters().size();
    }

    /**
     * Returns list of exceptions that can be thrown by this method.
     * If this method have no checked exceptions, the return value is a list of length 0.
     * The returned {@link List} is will always be instanced of UnmodifiableList.
     *
     * @return a list of exceptions
     */
    public List<LazyType> getExceptionTypes() {
        return exceptionTypes;
    }

    /**
     * @return {@link LazyType} instance containing return type of this method
     */
    public LazyType getReturnType() {
        return returnType;
    }
}
