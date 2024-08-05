package dev.velix.imperat.util.jeflect;


import java.lang.annotation.Annotation;
import java.util.List;

/**
 * It is an annotated program element contained in the byte code of the loaded class.
 */
public interface ByteAnnotated {

    /**
     * Returns this element's annotation for the specified type if such an annotation is present, else null.
     *
     * @param annotationClass the Class object corresponding to the annotation type
     * @return this element's annotation for the specified type if such an annotation is present, else null.
     */
    ByteAnnotation getAnnotation(Class<? extends Annotation> annotationClass);

    /**
     * Returns annotations that are present on this element.
     * If there are no annotations present on this element, the return value is a list of length 0.
     * The returned {@link List} is will always be instanced of UnmodifiableList.
     *
     * @return annotations present on this element
     */
    List<ByteAnnotation> getAnnotations();

    /**
     * Returns true if an annotation for the specified type is present on this element, else false.
     * This method is designed primarily for convenient access to marker annotations.
     *
     * @param annotationClass the Class object corresponding to the annotation type
     * @return true if an annotation for the specified annotation type is present on this element, else false
     */
    boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);
}
