package dev.velix.imperat.annotations.element;

import dev.velix.imperat.annotations.AnnotationRegistry;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
public final class MethodElement extends ParseElement<Method> {
    
    private final Set<ParameterElement> parameters = new LinkedHashSet<>();
    
    public MethodElement(
            @NotNull AnnotationRegistry registry,
            @Nullable ClassElement owningElement,
            @NotNull Method element
    ) {
        super(registry, owningElement, element);
        
        for (Parameter parameter : element.getParameters()) {
            parameters.add(new ParameterElement(registry, owningElement, this, parameter));
        }
    }
    
    
}
