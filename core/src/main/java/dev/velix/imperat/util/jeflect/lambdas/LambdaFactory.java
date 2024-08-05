package dev.velix.imperat.util.jeflect.lambdas;


import dev.velix.imperat.util.jeflect.DefineClassLoader;
import dev.velix.imperat.util.jeflect.DefineLoader;
import dev.velix.imperat.util.jeflect.DefineObjectFactory;
import dev.velix.imperat.util.jeflect.ObjectFactory;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * A factory that generates the bytecode of a proxy class for methods and constructors.
 */
public final class LambdaFactory {
	private static final String PROXY = "Proxy";
	private final ObjectFactory<Lambda> factory;

	public LambdaFactory(ObjectFactory<Lambda> factory) {
		this.factory = Objects.requireNonNull(factory);
	}

	public LambdaFactory(DefineLoader loader) {
		this(new DefineObjectFactory<>(loader));
	}

	public LambdaFactory() {
		this(new DefineClassLoader());
	}

	private static String getProxyName(Method method) {
		var toHash = method.getDeclaringClass().getName() + method.getName() + Type.getMethodDescriptor(method);
		return PROXY + toHash.hashCode();
	}

	private static String getProxyName(Constructor<?> ctor) {
		var toHash = ctor.getName() + Type.getConstructorDescriptor(ctor);
		return PROXY + toHash.hashCode();
	}


	public Lambda packMethod(Method method) {
		var name = getProxyName(method);
		return factory.create(name, () -> ProxyUtil.createProxy(name, method));
	}


	public Lambda packConstructor(Constructor<?> constructor) {
		var name = getProxyName(constructor);
		return factory.create(name, () -> ProxyUtil.createProxy(name, constructor));
	}
}
