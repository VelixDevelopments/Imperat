package dev.velix.imperat.annotations.parameters;

import dev.velix.imperat.command.parameters.CommandParameter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Represents an annotated parameter
 * from using annotation parser
 */
@ApiStatus.AvailableSince("1.0.0")
public interface AnnotatedParameter extends CommandParameter {
    
    /**
     * Get the instance of specific annotation
     *
     * @param clazz the type of annotation
     * @param <A>   the type of the annotation
     * @return the specific instance of an annotation of a certain type {@linkplain A}
     */
    @Nullable
    <A extends Annotation> A getAnnotation(Class<A> clazz);
    
    default boolean hasAnnotation(Class<? extends Annotation> clazz) {
        return getAnnotation(clazz) != null;
    }
    
    /**
     * @return the annotations associated with this parameter
     */
    Collection<? extends Annotation> getAnnotations();
    
    
}
