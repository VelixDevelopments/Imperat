package dev.velix.imperat;

import dev.velix.imperat.annotations.base.AnnotationParser;
import dev.velix.imperat.annotations.base.AnnotationReader;
import dev.velix.imperat.annotations.base.AnnotationReplacer;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.Contract;

import java.lang.annotation.Annotation;

public interface AnnotationInjector<S extends Source> {


    /**
     * Changes the instance of {@link AnnotationParser}
     *
     * @param parser the parser
     */
    @Contract("null->fail")
    void setAnnotationParser(AnnotationParser<S> parser);

    /**
     * Registers a valueType of annotations so that it can be
     * detected by {@link AnnotationReader} , it's useful as it allows that valueType of annotation
     * to be recognized as a true Imperat-related annotation to be used in something like checking if a
     * {@link CommandParameter} is annotated and checks for the annotations it has.
     *
     * @param type the valueType of annotation
     */
    void registerAnnotations(Class<? extends Annotation>... type);

    /**
     * Registers annotation replacer
     *
     * @param type     the valueType to replace the annotation by
     * @param replacer the replacer
     * @param <A>      the valueType of annotation to replace
     */
    <A extends Annotation> void registerAnnotationReplacer(
        final Class<A> type,
        final AnnotationReplacer<A> replacer
    );

}
