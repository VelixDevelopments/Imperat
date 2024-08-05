package dev.velix.imperat.util.jeflect;

import com.github.romanqed.jfunc.Function1;

import java.util.concurrent.Callable;

/**
 * An interface describing a universal define factory for proxy objects.
 *
 * @param <T> type of produced objects
 */
public interface ObjectFactory<T> {

	/**
	 * Creates a class object with the specified name,
	 * and if there are no loaded classes in the pool,
	 * creates it from bytes received from the specified provider.
	 *
	 * @param name     class name
	 * @param provider a byte code generator used in the absence of a class
	 * @param creator  a function that creates a class object
	 * @return created object
	 * @throws RuntimeException if there are problems during the search, generation or instantiation of the class
	 */
	T create(String name, Callable<byte[]> provider, Function1<Class<?>, ? extends T> creator);

	/**
	 * Creates a class object with the specified name,
	 * and if there are no loaded classes in the pool,
	 * creates it from bytes received from the specified provider.
	 *
	 * @param name     class name
	 * @param provider a byte code generator used in the absence of a class
	 * @return created object
	 * @throws RuntimeException if there are problems during the search, generation or instantiation of the class
	 */
	T create(String name, Callable<byte[]> provider);
}
