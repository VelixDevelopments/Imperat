package dev.velix.imperat.util.jeflect.meta;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * A class describing a factory that packages methods with a pre-known signature into lambda interfaces.
 */
public final class MetaFactory {
	private final MethodHandles.Lookup lookup;

	public MetaFactory(MethodHandles.Lookup lookup) {
		this.lookup = Objects.requireNonNull(lookup);
	}

	public MetaFactory() {
		this.lookup = MethodHandles.lookup();
	}

	/**
	 * @return {@link MethodHandles.Lookup} instance, which is used to package methods
	 */
	public MethodHandles.Lookup getLookup() {
		return lookup;
	}

	/**
	 * Extracts the type from the passed method.
	 *
	 * @param method method for extraction
	 * @return extracted type
	 * @throws RuntimeException if it couldn't access the method
	 */
	public MethodType extractType(Method method) {
		try {
			Objects.requireNonNull(method);
			var handle = lookup.unreflect(method);
			return handle.type();
		} catch (Error | RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Extracts the type from the passed dynamic method.
	 *
	 * @param method method for extraction
	 * @return extracted type
	 */
	public MethodType extractDynamicType(Method method) {
		return extractType(method).dropParameterTypes(0, 1);
	}

	/**
	 * Packages {@link MethodHandle} into the passed {@link LambdaType}.
	 *
	 * @param clazz  lambda class for packaging
	 * @param handle handle for packaging
	 * @param bind   instance of the object to which the packaged method will be bound
	 *               (if null, the method will be considered static)
	 * @param <T>    type of packing lambda
	 * @return the object instantiating the passed lambda
	 * @throws RuntimeException if any errors occurred during the packaging process
	 */
	@SuppressWarnings("unchecked")
	public <T> T packLambdaHandle(LambdaType<T> clazz, MethodHandle handle, Object bind) {
		Objects.requireNonNull(clazz);
		var lambdaMethod = clazz.getLambdaMethod();
		var lambdaType = extractDynamicType(lambdaMethod);
		var bindType = MethodType.methodType(clazz.getLambdaClass());
		var sourceType = handle.type();
		if (bind != null) {
			bindType = bindType.appendParameterTypes(bind.getClass());
			sourceType = sourceType.dropParameterTypes(0, 1);
		}
		try {
			var callSite = LambdaMetafactory.metafactory(
					  lookup,
					  lambdaMethod.getName(),
					  bindType,
					  lambdaType,
					  handle,
					  sourceType
			);
			var ret = bind == null ? callSite.getTarget() : callSite.getTarget().bindTo(bind);
			return (T) ret.invoke();
		} catch (Error | RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Automatically unreflects and packages {@link Method} into the passed {@link LambdaType}.
	 *
	 * @param clazz  lambda class for packaging
	 * @param method method for packaging
	 * @param bind   instance of the object to which the packaged method will be bound
	 *               (if null, the method will be considered static)
	 * @param <T>    type of packing lambda
	 * @return the object instantiating the passed lambda
	 * @throws RuntimeException if any errors occurred during the packaging process
	 */
	public <T> T packLambdaMethod(LambdaType<T> clazz, Method method, Object bind) {
		Objects.requireNonNull(method);
		try {
			var handle = lookup.unreflect(method);
			return packLambdaHandle(clazz, handle, bind);
		} catch (Error | RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Automatically unreflects and packages {@link Constructor} into the passed {@link LambdaType}.
	 *
	 * @param clazz       lambda class for packaging
	 * @param constructor constructor for packaging
	 * @param <T>         type of packing lambda
	 * @return the object instantiating the passed lambda
	 * @throws RuntimeException if any errors occurred during the packaging process
	 */
	public <T> T packLambdaConstructor(LambdaType<T> clazz, Constructor<?> constructor) {
		Objects.requireNonNull(constructor);
		try {
			var handle = lookup.unreflectConstructor(constructor);
			return packLambdaHandle(clazz, handle, null);
		} catch (Error | RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Automatically unreflects and packages static {@link Method} into the passed {@link LambdaType}.
	 *
	 * @param clazz  lambda class for packaging
	 * @param method method for packaging
	 * @param <T>    type of packing lambda
	 * @return the object instantiating the passed lambda
	 */
	public <T> T packLambdaMethod(LambdaType<T> clazz, Method method) {
		return packLambdaMethod(clazz, method, null);
	}
}
