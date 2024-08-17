package dev.velix.imperat.annotations;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public interface AnnotationReader {
	
	/**
	 * @return The class target
	 */
	@NotNull
	Class<?> getTargetClass();
	
	/**
	 * Get annotated element
	 * may be a parameter, method or even a class
	 *
	 * @param level the level
	 * @param key   the key of this element
	 * @return the annotated element
	 */
	@Nullable
	AnnotatedElement getAnnotated(AnnotationLevel level, ElementKey key);
	
	/**
	 * Fetches all annotations registered within an element
	 *
	 * @param level the level of this element
	 * @param key   the key of this element
	 * @return the annotations added to this element
	 */
	Collection<Annotation> getAnnotations(
					AnnotationLevel level,
					ElementKey key
	);
	
	/**
	 * Fetches the annotation of an element
	 *
	 * @param key   the element key
	 * @param level the level of the element
	 * @param type  the type of annotation
	 * @param <A>   the type of annotation
	 * @return the annotation added to the element
	 */
	@Nullable
	<A extends Annotation> A getAnnotation(
					ElementKey key,
					AnnotationLevel level,
					Class<A> type
	);
	
	record ElementKey(String id, String... signatureParamTypes) {
		public static ElementKey of(String id) {
			return new ElementKey(id);
		}
		
		@Override
		public String toString() {
			return "ElementKey{" +
							"id='" + id + '\'' +
							", signatureParams='" + String.join("->", signatureParamTypes) + "'";
		}
		
		@Override
		public boolean equals(Object object) {
			if (this == object) return true;
			if (object == null || getClass() != object.getClass()) return false;
			ElementKey key = (ElementKey) object;
			return Objects.equals(id, key.id) && (Objects.deepEquals(signatureParamTypes, key.signatureParamTypes));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(id, Arrays.hashCode(signatureParamTypes));
		}
	}
	
	static AnnotationReader read(CommandAnnotationRegistry registry, Class<?> target) {
		return new AnnotationReaderImpl(registry, target);
	}
}
