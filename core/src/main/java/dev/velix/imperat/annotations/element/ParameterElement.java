package dev.velix.imperat.annotations.element;

import dev.velix.imperat.annotations.AnnotationHelper;
import dev.velix.imperat.annotations.AnnotationRegistry;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Parameter;

@ApiStatus.Internal
@Getter
public final class ParameterElement extends ParseElement<Parameter> {

    private final String name;
    private final ClassElement owningClass;
    
    public ParameterElement(
            final AnnotationRegistry registry,
            final ClassElement owningClass,
            final MethodElement method,
            final Parameter element
    ) {
        super(registry, method, element);
        this.owningClass = owningClass;
        this.name = AnnotationHelper.getParamName(element);
    }
    
    @Override
    public String toString() {
        return getElement().getType().getSimpleName() + " " + name;
    }
    
}
