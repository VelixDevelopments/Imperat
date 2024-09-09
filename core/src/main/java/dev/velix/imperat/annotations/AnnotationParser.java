package dev.velix.imperat.annotations;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Source;

import java.lang.annotation.Annotation;

/**
 * Represents a class with a single responsibility of
 * parsing annotated command classes and translating/converting them
 * into {@link Command} POJOs then registering them using {@link Imperat}
 *
 * @param <S> the command-sender type
 */
public abstract class AnnotationParser<S extends Source> {

    protected final Imperat<S> dispatcher;

    AnnotationParser(Imperat<S> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static <S extends Source> AnnotationParser<S> defaultParser(Imperat<S> dispatcher) {
        return new AnnotationParserImpl<>(dispatcher);
    }

    /**
     * Parses annotated command class of type {@linkplain T}
     * into {@link Command} then register it using {@link Imperat}
     *
     * @param instance the instance of the command class
     * @param <T>      the type of annotated command class to parse
     */
    public abstract <T> void parseCommandClass(T instance);

    /**
     * Registers {@link AnnotationReplacer}
     *
     * @param type     the type to replace the annotation by
     * @param replacer the replacer
     */
    public abstract <A extends Annotation> void registerAnnotationReplacer(Class<A> type, AnnotationReplacer<A> replacer);

}
