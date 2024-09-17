package dev.velix.annotations;

import dev.velix.supplier.OptionalValueSupplier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER})
public @interface DefaultProvider {
    
    Class<? extends OptionalValueSupplier<?>> value();
}
