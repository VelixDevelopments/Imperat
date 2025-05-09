package dev.velix.imperat.annotations.base.element;

import dev.velix.imperat.annotations.Default;
import dev.velix.imperat.annotations.DefaultProvider;
import dev.velix.imperat.annotations.Flag;
import dev.velix.imperat.annotations.Switch;
import dev.velix.imperat.annotations.base.AnnotationHelper;
import dev.velix.imperat.annotations.base.AnnotationParser;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.annotations.ContextResolved;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public final class ParameterElement extends ParseElement<Parameter> {

    private final String name;
    private final Type type;
    private final ClassElement owningClass;
    private final boolean contextResolved; // Cached result of isContextResolved

    <S extends Source> ParameterElement(
        final AnnotationParser<S> parser,
        final ClassElement owningClass,
        final MethodElement method,
        final Parameter element
    ) {
        super(parser, method, element);
        this.owningClass = owningClass;
        this.name = AnnotationHelper.getParamName(parser.getImperat().config(), this);
        this.type = element.getParameterizedType();
        this.contextResolved = calculateIsContextResolved();
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

    public boolean isOptional() {
        return isAnnotationPresent(dev.velix.imperat.annotations.Optional.class)
                || isAnnotationPresent(Default.class)
                || isAnnotationPresent(DefaultProvider.class)
                || isAnnotationPresent(Flag.class)
                || isAnnotationPresent(Switch.class);
    }

    public boolean isContextResolved() {
        return contextResolved;
    }

    private boolean calculateIsContextResolved() {
        // Check if the parameter itself is annotated with @ContextResolved
        if (isAnnotationPresent(ContextResolved.class)) {
            return true;
        }

        // Check if the Type of the parameter is annotated with @ContextResolved
        if (type instanceof Class<?> clazz && clazz.isAnnotationPresent(ContextResolved.class)) {
            return true;
        }

        // Check if any of the annotations on the parameter are annotated with @ContextResolved
        for (final Annotation annotation : getDeclaredAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(ContextResolved.class)) {
                return true;
            }
        }

        return false;
    }
}
