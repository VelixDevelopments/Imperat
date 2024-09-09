package dev.velix.imperat.annotations.element;

import dev.velix.imperat.annotations.AnnotationReader;
import dev.velix.imperat.annotations.AnnotationRegistry;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Source;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

@Getter
public final class ClassElement extends ParseElement<Class<?>> {
    
    private final Set<ParseElement<?>> children = new LinkedHashSet<>();
    
    public ClassElement(
            @NotNull AnnotationRegistry registry,
            @Nullable ClassElement parent,
            @NotNull Class<?> element
    ) {
        super(registry, parent, element);
    }
    
    public <S extends Source> Set<Command<S>> accept(AnnotationReader<S> reader, CommandClassVisitor<S> visitor) {
        return visitor.visitCommandClass(reader, this);
    }
    
    
    public @Nullable ParseElement<?> getChildElement(Predicate<ParseElement<?>> predicate) {
        for (var element : getChildren())
            if (predicate.test(element)) return element;
        return null;
    }
    
    public void addChild(ParseElement<?> element) {
        children.add(element);
    }
    
    public boolean isRootClass() {
        return getParent() == null;
    }
    
    
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
}
