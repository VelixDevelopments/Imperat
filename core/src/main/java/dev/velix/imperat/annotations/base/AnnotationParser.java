package dev.velix.imperat.annotations.base;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.SubCommand;
import dev.velix.imperat.annotations.Usage;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;

/**
 * Represents a class with a single responsibility of
 * parsing annotated command classes and translating/converting them
 * into {@link Command} POJOs then registering them using {@link Imperat}
 *
 * @param <S> the command-sender type
 */
@Getter
public abstract class AnnotationParser<S extends Source> {

    protected final Imperat<S> imperat;

    AnnotationParser(Imperat<S> imperat) {
        this.imperat = imperat;
    }

    public static <S extends Source> AnnotationParser<S> defaultParser(
            @NotNull Imperat<S> dispatcher
    ) {
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
     * Registers a type of annotations so that it can be
     * detected by {@link AnnotationReader} , it's useful as it allows that type of annotation
     * to be recognized as a true Imperat-related annotation to be used in something like checking if a
     * {@link CommandParameter} is annotated and checks for the annotations it has.
     *
     * @param type the type of annotation
     */
    public abstract void registerAnnotations(Class<? extends Annotation>... type);

    /**
     * Registers {@link AnnotationReplacer}
     *
     * @param type     the type to replace the annotation by
     * @param replacer the replacer
     */
    public abstract <A extends Annotation> void registerAnnotationReplacer(Class<A> type, AnnotationReplacer<A> replacer);

    /**
     * Checks the internal registry whether the type of annotation entered is known/registered or not.
     *
     * @param annotationType the type of annotation to enter
     * @return whether the type of annotation entered is known/registered or not.
     */
    public abstract boolean isKnownAnnotation(Class<? extends Annotation> annotationType);

    /**
     * Checks if the specific type of annotation entered has a {@link AnnotationReplacer}
     * for it in the internal registry for replacers
     *
     * @param type the type of annotation entered
     * @return Whether the there's an annotation replacer for the type entered.
     */
    public abstract boolean hasAnnotationReplacerFor(Class<? extends Annotation> type);

    /**
     * Fetches the {@link AnnotationReplacer} mapped to the entered annotation type.
     *
     * @param type the type of annotation
     * @param <A>  the annotation type parameter
     * @return the {@link AnnotationReplacer} mapped to the entered annotation type.
     */
    public abstract <A extends Annotation> AnnotationReplacer<A> getAnnotationReplacer(Class<A> type);

    public final boolean isEntryPointAnnotation(Class<? extends Annotation> annotation) {
        return annotation == dev.velix.imperat.annotations.Command.class || annotation == Usage.class || annotation == SubCommand.class;
    }

}
