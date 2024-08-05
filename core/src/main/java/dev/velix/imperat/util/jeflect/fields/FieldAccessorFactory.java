package dev.velix.imperat.util.jeflect.fields;

import dev.velix.imperat.util.jeflect.DefineClassLoader;
import dev.velix.imperat.util.jeflect.DefineLoader;
import dev.velix.imperat.util.jeflect.DefineObjectFactory;
import dev.velix.imperat.util.jeflect.ObjectFactory;

import java.lang.reflect.Field;
import java.util.Objects;

public final class FieldAccessorFactory {
	private static final String ACCESSOR = "Accessor";
	private final ObjectFactory<FieldAccessor> factory;

	public FieldAccessorFactory(ObjectFactory<FieldAccessor> factory) {
		this.factory = Objects.requireNonNull(factory);
	}

	public FieldAccessorFactory(DefineLoader loader) {
		this(new DefineObjectFactory<>(loader));
	}

	public FieldAccessorFactory() {
		this(new DefineClassLoader());
	}

	public FieldAccessor packField(Field field) {
		var toHash = field.getDeclaringClass().getName() + field.getName();
		var name = ACCESSOR + toHash.hashCode();
		return factory.create(name, () -> FieldUtil.createAccessor(name, field));
	}
}
