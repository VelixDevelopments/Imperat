package dev.velix.imperat.annotations;

import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.command.Command;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;

/**
 * This class represents actions that are triggered
 * if an annotation is found to be used on an element
 * Most probably the actions will be based on taking information
 * from {@link Annotation} and put it into an existing object
 *
 * @param <O> object to load type
 * @param <C> command-sender type
 * @param <A> annotation to load type
 */
public interface AnnotationDataInjector<O, C, A extends Annotation> {

    /**
     * Injects the command-related object using the annotation
     * data that is loaded using {@link AnnotationReader}
     *
     * @param proxyInstance the instance of the class being  registered as a command class
     * @param proxy         class being registered as a command class
     * @param command       the created command instance owning
     *                      the plain class to load
     * @param toLoad        the object created to be modified and loaded
     * @param element       the element of parameter
     * @param annotation    the annotation to load
     */
    void inject(
            Object proxyInstance,
            Class<?> proxy,
            @NotNull Command<C> command,
            @NotNull O toLoad,
            AnnotationReader reader,
            @NotNull CommandAnnotatedElement<?> element,
            @NotNull A annotation
    );

}
