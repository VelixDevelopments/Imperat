package dev.velix.imperat.annotations.element;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.AnnotationReader;
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
    
    protected CommandClassVisitor(Imperat<S> imperat) {
        this.imperat = imperat;
    }
    
    public abstract Set<Command<S>> visitCommandClass(
            @NotNull AnnotationReader<S> reader,
            @NotNull ClassElement clazz
    );
    
    public static <S extends Source> CommandClassVisitor<S> newSimpleVisitor(
            Imperat<S> imperat
    ) {
        return new SimpleCommandClassVisitor<>(imperat);
    }
}
