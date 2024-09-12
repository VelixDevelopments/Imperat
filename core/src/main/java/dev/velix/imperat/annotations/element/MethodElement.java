package dev.velix.imperat.annotations.element;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.AnnotationHelper;
import dev.velix.imperat.annotations.AnnotationRegistry;
import dev.velix.imperat.context.Source;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

@Getter
public final class MethodElement extends ParseElement<Method> {
    
    private final List<ParameterElement> parameters = new ArrayList<>();
    private int inputCount = 0;
    private boolean help = false;
    
    public <S extends Source> MethodElement(
            @NotNull Imperat<S> imperat,
            @NotNull AnnotationRegistry registry,
            @Nullable ClassElement owningElement,
            @NotNull Method element
    ) {
        super(registry, owningElement, element);
        for (Parameter parameter : element.getParameters()) {
            
            if (AnnotationHelper.isHelpParameter(parameter)) {
                help = true;
            } else if (!imperat.canBeSender(parameter.getType())
                    && !imperat.hasContextResolver(parameter.getType())
            ) {
                inputCount++;
            }
            parameters.add(new ParameterElement(registry, owningElement, this, parameter));
        }
    }
    
    public @Nullable ParameterElement getParameterAt(int index) {
        if (index < 0 || index >= size()) return null;
        return parameters.get(index);
    }
    
    
    public int size() {
        return parameters.size();
    }
    
}
