package dev.velix.imperat.annotations.element;

import dev.velix.imperat.annotations.AnnotationRegistry;
import dev.velix.imperat.annotations.AnnotationReplacer;
import dev.velix.imperat.util.annotations.AnnotationMap;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Iterator;

@ApiStatus.Internal
public class CommandAnnotatedElement<E extends AnnotatedElement> implements AnnotatedElement, Iterable<Annotation> {

    private final @NotNull AnnotationMap map = new AnnotationMap();

    @Getter
    private final @NotNull E element;

    public CommandAnnotatedElement(
            @NotNull AnnotationRegistry registry,
            @NotNull E element
    ) {
        this.element = element;
        this.load(registry);
    }

    @SuppressWarnings("unchecked")
    private <A extends Annotation> void load(@NotNull AnnotationRegistry registry) {
        for (Annotation annotation : element.getDeclaredAnnotations()) {
            Class<A> clazz = (Class<A>) annotation.annotationType();
            if (registry.isRegisteredType(clazz)) {
                map.set(annotation);
            } else if (registry.hasReplacerFor(clazz)) {
                AnnotationReplacer<A> replacer = registry.getAnnotationReplacer(clazz);
                assert replacer != null;
                replacer.replace((A) annotation)
                        .forEach(map::set);
            }

        }
    }

    /**
     * Returns this element's annotation for the specified type if
     * such an annotation is <em>present</em>, else null.
     *
     * @param annotationClass the Class object corresponding to the
     *                        annotation type
     * @return this element's annotation for the specified annotation type if
     * present on this element, else null
     * @throws NullPointerException if the given annotation class is null
     * @since 1.5
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getAnnotation(@NotNull Class<T> annotationClass) {
        return (T) map.get(annotationClass);
    }

    /**
     * Returns annotations that are <em>present</em> on this element.
     * <p>
     * If there are no annotations <em>present</em> on this element, the return
     * value is an array of length 0.
     * <p>
     * The caller of this method is free to modify the returned array; it will
     * have no effect on the arrays returned to other callers.
     *
     * @return annotations present on this element
     * @since 1.5
     */
    @Override
    public Annotation[] getAnnotations() {
        return map.values().toArray(new Annotation[0]);
    }

    /**
     * Returns annotations that are <em>directly present</em> on this element.
     * This method ignores inherited annotations.
     * <p>
     * If there are no annotations <em>directly present</em> on this element,
     * the return value is an array of length 0.
     * <p>
     * The caller of this method is free to modify the returned array; it will
     * have no effect on the arrays returned to other callers.
     *
     * @return annotations directly present on this element
     * @since 1.5
     */
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return map.values().toArray(new Annotation[0]);
    }


    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    @Override
    public Iterator<Annotation> iterator() {
        return map.values().iterator();
    }

    @Override
    public String toString() {
        if (element instanceof Method method) {
            return method.getName();
        }
        if (element instanceof Class<?> clazz) {
            return clazz.getSimpleName();
        }
        return element.toString();
    }
}
