package dev.velix.annotations.base;

import dev.velix.Imperat;
import dev.velix.annotations.base.element.CommandClassVisitor;
import dev.velix.annotations.base.element.RootCommandClass;
import dev.velix.context.Source;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a class that has a single responsibility of
 * reading the annotation components of an annotated command class
 *
 * @author Mqzen
 */
@ApiStatus.AvailableSince("1.0.0")
public interface AnnotationReader<S extends Source> {
    
    static <S extends Source> AnnotationReader<S> read(Imperat<S> imperat, AnnotationRegistry registry, Object target) {
        return new AnnotationReaderImpl<>(imperat, registry, target);
    }
    
    RootCommandClass<S> getRootClass();
    
    void accept(Imperat<S> imperat, CommandClassVisitor<S> visitor);
    
    
}
