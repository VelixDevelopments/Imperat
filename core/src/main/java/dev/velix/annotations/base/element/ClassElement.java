package dev.velix.annotations.base.element;

import dev.velix.annotations.base.AnnotationRegistry;
import dev.velix.command.Command;
import dev.velix.context.Source;
import dev.velix.util.reflection.Reflections;
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
    
    public ClassElement(
            @NotNull AnnotationRegistry registry,
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
            //System.out.println("Starting to visit class");
            return visitor.visitCommandClass(this);
        } catch (Throwable ex) {
            //System.out.println("some exception happened");
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
    
    
}
