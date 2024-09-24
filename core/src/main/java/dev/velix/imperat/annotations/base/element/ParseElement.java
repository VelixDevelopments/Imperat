package dev.velix.imperat.annotations.base.element;

import dev.velix.imperat.annotations.base.AnnotationRegistry;
import dev.velix.imperat.annotations.base.AnnotationReplacer;
import dev.velix.imperat.util.AnnotationMap;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Objects;

@ApiStatus.Internal
public sealed abstract class ParseElement<E extends AnnotatedElement> implements AnnotatedElement, Iterable<Annotation> permits ClassElement, MethodElement, ParameterElement {
    
    //use more memory but better performance, since it's going to be GCed anyway
    private final @NotNull AnnotationMap total = new AnnotationMap();
    
    @Getter
    private final @Nullable ParseElement<?> parent;
    
    @Getter
    private final @NotNull E element;
    
    public ParseElement(
            @NotNull AnnotationRegistry registry,
            @Nullable ParseElement<?> parent,
            @NotNull E element
    ) {
        this.parent = parent;
        this.element = element;
        this.load(registry);
    }
    
    @SuppressWarnings("unchecked")
    private <A extends Annotation> void load(@NotNull AnnotationRegistry registry) {
        for (Annotation annotation : element.getDeclaredAnnotations()) {
            Class<A> clazz = (Class<A>) annotation.annotationType();
            if (registry.isRegisteredAnnotation(clazz)) {
                total.put(clazz, annotation);
            } else if (registry.hasReplacerFor(clazz)) {
                //we add the custom annotation anyway
                total.put(clazz, annotation);
                
                //adding the replaced annotations
                AnnotationReplacer<A> replacer = registry.getAnnotationReplacer(clazz);
                assert replacer != null;
                replacer.replace((A) annotation)
                        .forEach((replacedAnnotation) -> total.put(clazz, annotation));
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
    public <T extends Annotation> @Nullable T getAnnotation(@NotNull Class<T> annotationClass) {
        return (T) total.get(annotationClass);
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
        return total.values().toArray(new Annotation[0]);
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
        return total.values().toArray(new Annotation[0]);
    }
    
    
    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    @Override
    public Iterator<Annotation> iterator() {
        return total.values().iterator();
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParseElement<?> that)) return false;
        return Objects.equals(parent, that.parent)
                && Objects.equals(element, that.element);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(parent, element);
    }
    
    public abstract String getName();
}
