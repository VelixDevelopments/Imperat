package dev.velix.imperat.annotations.base.element;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.base.AnnotationRegistry;
import dev.velix.imperat.annotations.base.element.selector.ElementSelector;
import dev.velix.imperat.annotations.base.element.selector.MethodRules;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Visits each element in a {@link ClassElement}
 * @param <S> the command source
 */
public abstract class CommandClassVisitor<S extends Source> {
    
    protected final Imperat<S> imperat;
    protected final AnnotationRegistry registry;
    protected final ElementSelector<MethodElement> methodSelector;
    
    protected CommandClassVisitor(Imperat<S> imperat, AnnotationRegistry registry, ElementSelector<MethodElement> methodSelector) {
        this.imperat = imperat;
        this.registry = registry;
        this.methodSelector = methodSelector;
    }
    
    public abstract Set<Command<S>> visitCommandClass(
            @NotNull ClassElement clazz
    );
    
    public static <S extends Source> CommandClassVisitor<S> newSimpleVisitor(
            Imperat<S> imperat,
            AnnotationRegistry registry
    ) {
        return new SimpleCommandClassVisitor<>(
                imperat,
                registry,
                ElementSelector.<MethodElement>create()
                        .addRule(MethodRules.HAS_KNOWN_SENDER)
                        .addRule(MethodRules.HAS_LEAST_ONLY_ONE_MAIN_ANNOTATION)
        );
    }
}
