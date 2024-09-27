package dev.velix.imperat.annotations.base.element;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.base.AnnotationParser;
import dev.velix.imperat.annotations.base.element.selector.ElementSelector;
import dev.velix.imperat.annotations.base.element.selector.MethodRules;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Visits each element in a {@link ClassElement}
 *
 * @param <S> the command source
 */
public abstract class CommandClassVisitor<S extends Source> {

    protected final Imperat<S> imperat;
    protected final AnnotationParser<S> parser;
    protected final ElementSelector<MethodElement> methodSelector;

    protected CommandClassVisitor(
            Imperat<S> imperat,
            AnnotationParser<S> parser,
            ElementSelector<MethodElement> methodSelector
    ) {
        this.imperat = imperat;
        this.parser = parser;
        this.methodSelector = methodSelector;
    }

    public abstract Set<Command<S>> visitCommandClass(
            @NotNull ClassElement clazz
    );

    public static <S extends Source> CommandClassVisitor<S> newSimpleVisitor(
            Imperat<S> imperat,
            AnnotationParser<S> parser
    ) {
        return new SimpleCommandClassVisitor<>(
                imperat,
                parser,
                ElementSelector.<MethodElement>create()
                        .addRule(MethodRules.HAS_KNOWN_SENDER)
                        .addRule(MethodRules.HAS_LEAST_ONLY_ONE_MAIN_ANNOTATION)
        );
    }
}
