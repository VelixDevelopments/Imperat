package dev.velix.imperat.annotations.base;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.base.element.CommandClassVisitor;
import dev.velix.imperat.annotations.base.element.MethodElement;
import dev.velix.imperat.annotations.base.element.RootCommandClass;
import dev.velix.imperat.annotations.base.element.selector.ElementSelector;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a class that has a single responsibility of
 * reading the annotation components of an annotated command class
 *
 * @author Mqzen
 */
@ApiStatus.AvailableSince("1.0.0")
public interface AnnotationReader<S extends Source> {
    
    static <S extends Source> AnnotationReader<S> read(
            Imperat<S> imperat,
            ElementSelector<MethodElement> methodSelector,
            AnnotationRegistry registry,
            Object target
    ) {
        return new AnnotationReaderImpl<>(imperat, methodSelector, registry, target);
    }
    
    RootCommandClass<S> getRootClass();
    
    void accept(Imperat<S> imperat, CommandClassVisitor<S> visitor);
    
    
}
