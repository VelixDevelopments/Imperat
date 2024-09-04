package dev.velix.imperat.annotations;

import dev.velix.imperat.annotations.element.ElementKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;

/**
 * Represents a class that has a single responsibility of
 * reading the annotation components of an annotated command class
 * Each annotation component of specific {@link AnnotationLevel} is packed
 * into a containing class {@link AnnotationContainer}
 *
 * @author Mqzen
 */
@ApiStatus.AvailableSince("1.0.0")
public interface AnnotationReader {

    /**
     * Get an annotated element
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


    static AnnotationReader read(AnnotationRegistry registry, Class<?> target) {
        return new AnnotationReaderImpl(registry, target);
    }
}
