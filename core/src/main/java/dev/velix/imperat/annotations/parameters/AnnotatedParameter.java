package dev.velix.imperat.annotations.parameters;

import dev.velix.imperat.command.parameters.UsageParameter;
import dev.velix.imperat.util.AnnotationMap;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Collection;

/**
 * Represents an annotated parameter
 * from using annotation parser
 * @see dev.velix.imperat.AnnotationParser
 */
public interface AnnotatedParameter extends UsageParameter {

	@Nullable
	<A extends Annotation> A getAnnotation(Class<A> clazz);

	default boolean hasAnnotation(Class<? extends Annotation> clazz) {
		return getAnnotation(clazz) != null;
	}

	Collection<? extends Annotation> getAnnotations();

	static AnnotatedParameter flag(String flag, Parameter parameter) {
		return new AnnotatedFlagParameter(flag, AnnotationMap.loadFrom(parameter));
	}

	static AnnotatedParameter input(String name,
	                                Class<?> type,
	                                boolean optional,
											  boolean greedy,
											  Object defaultValue,
	                                Parameter parameter) {
		return new AnnotatedNormalParameter(name, type, optional, greedy,
				  defaultValue, AnnotationMap.loadFrom(parameter));
	}

}
