package dev.velix.imperat.annotations;

import dev.velix.imperat.command.processors.CommandPostProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface PostProcessor {
	
	Class<? extends CommandPostProcessor<?>> value();
	
}
