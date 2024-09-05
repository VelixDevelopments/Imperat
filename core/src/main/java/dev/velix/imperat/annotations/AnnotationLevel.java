package dev.velix.imperat.annotations;

import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.element.ElementVisitor;
import dev.velix.imperat.annotations.element.MethodParameterElement;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Represents a place where the annotations
 * can be place
 */
@ApiStatus.Internal
public enum AnnotationLevel {
    
    
    CLASS(ElementVisitor::classes) {
        @Override
        public boolean matches(AnnotatedElement element) {
            return element instanceof CommandAnnotatedElement<?> cmdElement
                    && cmdElement.getElement() instanceof Class<?>;
        }
        
        @Override
        public Set<AnnotatedElement> getElements(AnnotationRegistry registry, Class<?> target) {
            return Set.of(new CommandAnnotatedElement<>(registry, target));
        }
    },
    
    METHOD(ElementVisitor::methods) {
        @Override
        public boolean matches(AnnotatedElement element) {
            return element instanceof CommandAnnotatedElement<?> cmdElement
                    && cmdElement.getElement() instanceof Method;
        }
        
        @Override
        public Set<AnnotatedElement> getElements(AnnotationRegistry registry,
                                                 Class<?> target) {
            Set<AnnotatedElement> elements = new HashSet<>();
            for (Method method : target.getDeclaredMethods()) {
                elements.add(new CommandAnnotatedElement<>(registry, method));
            }
            return elements;
        }
    },
    
    PARAMETER(ElementVisitor::parameters) {
        @Override
        public boolean matches(AnnotatedElement element) {
            return element instanceof CommandAnnotatedElement<?> cmdElement
                    && cmdElement.getElement() instanceof Parameter;
        }
        
        @Override
        public Set<AnnotatedElement> getElements(AnnotationRegistry registry, Class<?> target) {
            Set<AnnotatedElement> elements = new HashSet<>();
            for (Method method : target.getDeclaredMethods()) {
                for (Parameter parameter : method.getParameters()) {
                    MethodParameterElement element = new MethodParameterElement(registry, parameter);
                    elements.add(element);
                }
            }
            return elements;
        }
    },
    
    WILDCARD(() -> null) {
        @Override
        public boolean matches(AnnotatedElement element) {
            return true;
        }
        
        @Override
        public Set<? extends AnnotatedElement> getElements(
                AnnotationRegistry registry,
                Class<?> target
        ) {
            Set<AnnotatedElement> elements = new HashSet<>();
            for (AnnotationLevel level : AnnotationLevel.values()) {
                if (level == WILDCARD) continue;
                elements.addAll(level.getElements(registry, target));
            }
            return elements;
        }
    };
    
    
    private final Supplier<ElementVisitor<?>> supplier;
    
    AnnotationLevel(Supplier<ElementVisitor<?>> visitorSupplier) {
        this.supplier = visitorSupplier;
    }
    
    ElementVisitor<?> getVisitor() {
        return supplier.get();
    }
    
    public abstract boolean matches(AnnotatedElement element);
    
    public abstract Set<? extends AnnotatedElement> getElements(AnnotationRegistry registry,
                                                                Class<?> target);
}
