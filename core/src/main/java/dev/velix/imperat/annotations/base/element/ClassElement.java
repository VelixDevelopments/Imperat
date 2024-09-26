package dev.velix.imperat.annotations.base.element;

import dev.velix.imperat.annotations.base.AnnotationParser;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.reflection.Reflections;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

@Getter
public final class ClassElement extends ParseElement<Class<?>> {
    
    private final Set<ParseElement<?>> children = new LinkedHashSet<>();
    
    public <S extends Source> ClassElement(
            @NotNull AnnotationParser<S> registry,
            @Nullable ClassElement parent,
            @NotNull Class<?> element
    ) {
        super(registry, parent, element);
    }
    
    public Object newInstance(Object... constructorArgs) {
        Class<?>[] types = new Class[constructorArgs.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = constructorArgs[i].getClass();
        }
        
        try {
            Constructor<?> cons = Reflections.getConstructor(this.getElement(), types);
            if (cons == null) {
                throw new IllegalCallerException("Class " + this.getElement().getSimpleName() + " doesn't have a constructor matching the arguments");
            }
            return cons.newInstance(constructorArgs);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    public <S extends Source> Set<Command<S>> accept(CommandClassVisitor<S> visitor) {
        try {
            return visitor.visitCommandClass(this);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return Collections.emptySet();
        }
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
    public String getName() {
        return getElement().getName();
    }
    
    public String getSimpleName() {
        return getElement().getSimpleName();
    }
}
