package dev.velix.imperat.annotations.base;

import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.base.element.ParseElement;
import org.jetbrains.annotations.*;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Replaces a custom annotation made by the user, with annotations
 * made from the basic pre-made annotations such as {@link Command}
 *
 * @param <A> the valueType of annotation to replace with other annotations
 */
@ApiStatus.AvailableSince("1.0.0")
public interface AnnotationReplacer<A extends Annotation> {

    /**
     * The annotation to replace
     *
     * @param element the loaded element holding this annotation
     * @param annotation the annotation
     * @return the annotations replaced by this annotation
     */
    @NotNull
    Collection<Annotation> replace(@NotNull ParseElement<?> element, A annotation);

}
