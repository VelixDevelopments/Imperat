package dev.velix.imperat.annotations;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.command.Command;

import java.lang.annotation.Annotation;

/**
 * Represents a class with a single responsibility of
 * parsing annotated command classes and translating/converting them
 * into {@link Command} POJOs then registering them using {@link Imperat}
 *
 * @param <C> the command-sender type
 */
public abstract class AnnotationParser<C> {

    protected final Imperat<C> dispatcher;

    AnnotationParser(Imperat<C> dispatcher) {
        this.dispatcher = dispatcher;
    }
    
    /**
     * Parses annotated command class of type {@linkplain T}
     * into {@link Command} then register it using {@link Imperat}
     *
     * @param instance the instance of the command class
     * @param <T>      the type of annotated command class to parse
     */
    public abstract <T> Command<C> parseCommandClass(T instance);
    
    /**
     * Parses annotated command class of type {@linkplain T}
     * into {@link Command} then register it using {@link Imperat}
     *
     * @param instance the instance of the command class
     * @param <T>      the type of annotated command class to parse
     */
    public abstract <T> Command<C> parseCommandClass(
            dev.velix.imperat.annotations.types.Command commandAnnotation,
            AnnotationReader reader,
            CommandAnnotatedElement<?> element,
            T instance,
            Class<T> instanceClass
    );

    
    
    /**
     * Registers {@link AnnotationReplacer}
     *
     * @param type     the type to replace the annotation by
     * @param replacer the replacer
     */
    public abstract <A extends Annotation> void registerAnnotationReplacer(Class<A> type, AnnotationReplacer<A> replacer);

    public static <C> AnnotationParser<C> defaultParser(Imperat<C> dispatcher) {
        return new AnnotationParserImpl<>(dispatcher);
    }

}
