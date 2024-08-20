package dev.velix.imperat.annotations.types.parameters;

import dev.velix.imperat.resolvers.OptionalValueSupplier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER})
public @interface DefaultValueProvider {
	
	Class<? extends OptionalValueSupplier<?>> value();
}
