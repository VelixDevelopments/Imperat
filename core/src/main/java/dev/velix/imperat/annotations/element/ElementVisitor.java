package dev.velix.imperat.annotations.element;

import dev.velix.imperat.annotations.AnnotationContainer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * Represents a visitor for {@link AnnotatedElement}
 * which adds the elements directly into the container
 * this allows for high flexibility when dealing with this interface
 * as you can customize easily how the elements are added into the container
 * providing very high scalability
 *
 * @param <E> type of element that contain annotations
 */
public interface ElementVisitor<E extends AnnotatedElement> {

    /**
     * Loads the element unique key from the element type
     *
     * @param element the element
     * @return the element unique key
     */
    ElementKey loadKey(E element);

    /**
     * Visits the annotation container and an element in
     * the class to parse, allowing for more flexibility.
     * so it adds a behaviour without modifying the internals of {@link AnnotationContainer}
     * by going through {@link AnnotationContainer#accept(ElementVisitor, CommandAnnotatedElement)}
     *
     * @param container the container
     * @param element   the element
     */
    default void visit(AnnotationContainer container, @NotNull CommandAnnotatedElement<E> element) {
        var key = loadKey(element.getElement());
        container.addElement(key, element);
    }

    static ElementVisitor<Class<?>> classes() {
        return (element) -> ElementKey.of(element.getName());
    }

    static ElementVisitor<Method> methods() {
        return (element) -> new ElementKey(element.getName(),
                Arrays.stream(element.getParameterTypes())
                        .map(Class::getName).toArray(String[]::new));
    }

    static ElementVisitor<Parameter> parameters() {
        return (param) -> new ElementKey(param.getName(),
                param.getType().getName());
    }

}
