package dev.velix.imperat.annotations.base.element;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.base.AnnotationHelper;
import dev.velix.imperat.annotations.base.AnnotationParser;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class MethodElement extends ParseElement<Method> {

    private final List<ParameterElement> parameters = new ArrayList<>();
    private int inputCount = 0;
    private boolean help = false;

    public <S extends Source> MethodElement(
        @NotNull Imperat<S> imperat,
        @NotNull AnnotationParser<S> registry,
        @Nullable ClassElement owningElement,
        @NotNull Method element
    ) {
        super(registry, owningElement, element);
        var params = element.getParameters();
        for(int i = 1; i< params.length; i++) {
            Parameter parameter = params[i];
            if (AnnotationHelper.isHelpParameter(parameter)) {
                help = true;
            } else if (!imperat.config().hasContextResolver(parameter.getType())) {
                inputCount++;
            }
        }
        for(Parameter parameter : params)
            parameters.add(new ParameterElement(registry, owningElement, this, parameter));

    }

    public @Nullable ParameterElement getParameterAt(int index) {
        if (index < 0 || index >= size()) return null;
        return parameters.get(index);
    }


    public int size() {
        return parameters.size();
    }

    public Type getReturnType() {
        return getElement().getReturnType();
    }

    public int getModifiers() {
        return getElement().getModifiers();
    }

    @Override
    public String getName() {
        return getElement().getName();
    }

    public List<ParameterElement> getParameters() {
        return parameters;
    }

    public int getInputCount() {
        return inputCount;
    }

    public boolean isHelp() {
        return help;
    }

}
