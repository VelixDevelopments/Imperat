package dev.velix.imperat.annotations.types.parameters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER})
public @interface Range {
	
	//TODO implement to use !
	double min() default Double.MIN_VALUE;
	
	double max() default Double.MAX_VALUE;
	
}
