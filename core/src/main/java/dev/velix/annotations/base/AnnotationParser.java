package dev.velix.annotations.base;

import dev.velix.Imperat;
import dev.velix.annotations.base.element.CommandClassVisitor;
import dev.velix.command.Command;
import dev.velix.context.Source;
import org.jetbrains.annotations.NotNull;

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
    
    public static <S extends Source> AnnotationParser<S> defaultParser(@NotNull Imperat<S> dispatcher, @NotNull CommandClassVisitor<S> visitor) {
        return new AnnotationParserImpl<>(dispatcher, visitor);
    }
    
    public static <S extends Source> AnnotationParser<S> defaultParser(@NotNull Imperat<S> dispatcher) {
        return defaultParser(dispatcher, CommandClassVisitor.newSimpleVisitor(dispatcher));
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
