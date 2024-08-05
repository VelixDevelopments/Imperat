package dev.velix.imperat.util.jeflect;

import com.github.romanqed.jfunc.LazySupplier;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * A class representing a lazy reference to an entry in an enumeration.
 */
public final class EnumEntry {
	private final LazyType type;
	private final Supplier<Object> fieldSupplier;

	public EnumEntry(String className, String field) {
		this.type = new LazyType(className);
		this.fieldSupplier = new LazySupplier<>(() -> {
			try {
				return type.getType().getField(field);
			} catch (NoSuchFieldException e) {
				throw new NoSuchElementException("Enum " + type.getTypeName() + "not contains entry " + field);
			}
		});
	}

	/**
	 * Loads an enum class and returns an instance of {@link Class} containing information about it.
	 *
	 * @return {@link Class} that represents enum class
	 */
	public Class<?> getType() {
		return type.getType();
	}

	/**
	 * Returns enum class name.
	 *
	 * @return enum class name
	 */
	public String getTypeName() {
		return type.getTypeName();
	}

	/**
	 * Loads an enum class and access its field, containing referenced entry.
	 *
	 * @param <T> enum type
	 * @return enum element
	 */
	@SuppressWarnings("unchecked")
	public <T> T getEntry() {
		return (T) fieldSupplier.get();
	}
}
