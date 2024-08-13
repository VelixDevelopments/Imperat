package dev.velix.imperat.annotations;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;

public interface AnnotationReader {

	/**
	 * @return The class target
	 */
	@NotNull Class<?> getTargetClass();

	/**
	 * Get annotated element
	 * may be a parameter, method or even a class
	 * @param level the level
	 * @param key the key of this element
	 * @return the annotated element
	 */
	@Nullable
	AnnotatedElement getAnnotated(AnnotationLevel level, ElementKey key);

	/**
	 * Fetches all annotations registered within an element
	 * @param level the level of this element
	 * @param key the key of this element
	 * @return the annotations added to this element
	 */
	Collection<Annotation> getAnnotations(
			  AnnotationLevel level,
			  ElementKey key
	);

	/**
	 * Fetches the annotation of an element
	 * @param key the element key
	 * @param level the level of the element
	 * @param type the type of annotation
	 *
	 * @return the annotation added to the element
	 * @param <A> the type of annotation
	 */
	@Nullable
	<A extends Annotation> A getAnnotation(
			  ElementKey key,
			  AnnotationLevel level,
			  Class<A> type
	);

	record ElementKey(String id, Class<?>... signatureParams) {

	}

	static AnnotationReader read(CommandAnnotationRegistry registry, Class<?> target) {
		return new AnnotationReaderImpl(registry, target);
	}
}
