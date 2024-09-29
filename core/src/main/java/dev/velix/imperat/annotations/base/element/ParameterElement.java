package dev.velix.imperat.annotations.base.element;

import dev.velix.imperat.annotations.base.AnnotationHelper;
import dev.velix.imperat.annotations.base.AnnotationParser;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

@ApiStatus.Internal
public final class ParameterElement extends ParseElement<Parameter> {

    private final String name;
    private final Type type;
    private final ClassElement owningClass;

    <S extends Source> ParameterElement(
            final AnnotationParser<S> parser,
            final ClassElement owningClass,
            final MethodElement method,
            final Parameter element
    ) {
        super(parser, method, element);
        this.owningClass = owningClass;
        this.name = AnnotationHelper.getParamName(parser.getImperat(), element);
        this.type = element.getParameterizedType();
    }

    @Override
    public String toString() {
        return getElement().getType().getSimpleName() + " " + name;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public ClassElement getOwningClass() {
        return owningClass;
    }

}
