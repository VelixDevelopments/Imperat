package dev.velix.imperat.util.jeflect;

/**
 * An interface describing the mechanism that loads a class into memory.
 */
public interface DefineLoader {
	/**
	 * Defines a class in memory and returns its instance.
	 *
	 * @param name   the name with which the class will be defined
	 * @param buffer buffer containing the byte code of the class
	 * @return the {@link Class} containing the loaded class
	 */
	Class<?> define(String name, byte[] buffer);

	/**
	 * Loads the class with the specified binary name.
	 *
	 * @param name the binary name of the class
	 * @return the resulting {@link Class} object, or null if class not found
	 */
	Class<?> load(String name);

	/**
	 * @return the classloader used
	 */
	ClassLoader getClassLoader();
}
