package dev.velix.imperat.annotations.base.element;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.ContextResolved;
import dev.velix.imperat.annotations.base.AnnotationHelper;
import dev.velix.imperat.annotations.base.AnnotationParser;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.ImperatDebugger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class MethodElement extends ParseElement<Method> {

    private final List<ParameterElement> parameters = new ArrayList<>();
    private int inputCount = 0;
    private int optionalCount = 0;

    private boolean help = false;

    public <S extends Source> MethodElement(
        @NotNull Imperat<S> imperat,
        @NotNull AnnotationParser<S> registry,
        @Nullable ClassElement owningElement,
        @NotNull Method element
    ) {
        super(registry, owningElement, element);
        var params = element.getParameters();
        for (int i = 0; i < params.length; i++) {
            var parameter = params[i];
            //TODO debug this !
            ParameterElement parameterElement = new ParameterElement(registry, owningElement, this, parameter);
            ImperatDebugger.debug("Adding param '%s' to method '%s'", parameterElement.getName(), this.getName());
            parameters.add(parameterElement);
            if (i > 0 ) {

                if(!parameterElement.isAnnotationPresent(ContextResolved.class)) {
                    inputCount++;
                    if(parameterElement.isOptional()) {
                        optionalCount++;
                    }
                }

                if(!help && AnnotationHelper.isHelpParameter(parameterElement)) {
                    help = true;
                }

            }
        }

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

    public boolean isAllOptionalInput() {
        return inputCount == optionalCount;
    }

    public boolean isHelp() {
        return help;
    }

    @Override
    public @NotNull ParseElement<?> getParent() {
        assert super.getParent() != null;
        return super.getParent();
    }
}
