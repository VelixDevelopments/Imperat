package dev.velix.imperat.annotations.element;

import dev.velix.imperat.annotations.AnnotationHelper;
import dev.velix.imperat.annotations.AnnotationRegistry;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Parameter;

@ApiStatus.Internal
@Getter
public final class MethodParameterElement extends CommandAnnotatedElement<Parameter> {

    private final String name;

    public MethodParameterElement(
            final AnnotationRegistry registry,
            final Parameter element
    ) {
        super(registry, element);
        this.name = AnnotationHelper.getParamName(element);
    }

    @Override
    public String toString() {
        return getElement().getType().getSimpleName() + " " + name;
    }
}
