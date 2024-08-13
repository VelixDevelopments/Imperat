package dev.velix.imperat.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Replaces a custom annotation made by the user , with annotations
 * made from the basic pre-made annotations such as {@link dev.velix.imperat.annotations.types.Command}
 * @param <A> the type of annotation to replace with other annotations
 */
public interface AnnotationReplacer<A extends Annotation> {

	/**
	 * The annotation to replace
	 * @param annotation the annotation
	 * @return the annotations replaced by this annotation
	 */
	@NotNull
	Collection<Annotation> replace(A annotation);

}
