package dev.velix.imperat.annotations.base.element;

import dev.velix.imperat.annotations.base.AnnotationHelper;
import dev.velix.imperat.annotations.base.AnnotationParser;
import dev.velix.imperat.context.Source;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

@ApiStatus.Internal
@Getter
public final class ParameterElement extends ParseElement<Parameter> {

    private final String name;
    private final Type type;
    private final ClassElement owningClass;

    <S extends Source> ParameterElement(
            final AnnotationParser<S> registry,
            final ClassElement owningClass,
            final MethodElement method,
            final Parameter element
    ) {
        super(registry, method, element);
        this.owningClass = owningClass;
        this.name = AnnotationHelper.getParamName(element);
        this.type = element.getParameterizedType();
    }

    @Override
    public String toString() {
        return getElement().getType().getSimpleName() + " " + name;
    }

}
