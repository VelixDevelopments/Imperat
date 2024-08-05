package dev.velix.imperat.util.jeflect;

import org.objectweb.asm.Type;

/**
 * A class representing a method parameter.
 */
public abstract class ByteParameter extends AbstractAnnotated {
	private final String descriptor;
	private final LazyType type;
	private final int modifiers;

	public ByteParameter(String descriptor, int modifiers) {
		this.descriptor = descriptor;
		this.type = new LazyType(Type.getType(descriptor).getClassName());
		this.modifiers = modifiers;
	}

	/**
	 * Loads a return type and returns an instance of {@link Class} containing information about it.
	 *
	 * @return {@link Class} that represents return type
	 */
	public Class<?> getType() {
		return type.getType();
	}

	/**
	 * @return the name of the return type
	 */
	public String getTypeName() {
		return type.getTypeName();
	}

	/**
	 * @return modifiers of this parameter
	 */
	public int getModifiers() {
		return modifiers;
	}

	/**
	 * @return descriptor of this parameter
	 */
	public String getDescriptor() {
		return descriptor;
	}
}
