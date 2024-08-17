package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;

public interface AnnotationDataCreator<O, A extends Annotation> {
	
	/**
	 * Creates a data instance from the annotation
	 *
	 * @param annotation the annotation
	 * @return the object to create such as {@link Command} {@link CommandUsage}
	 */
	@NotNull
	O create(Class<?> proxy,
	         A annotation,
	         CommandAnnotatedElement<?> element);
	
}
