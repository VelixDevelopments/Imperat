package dev.velix.imperat.annotations.parameters;

import dev.velix.imperat.annotations.base.element.ParameterElement;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.command.parameters.InputParameter;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

public final class AnnotationParameterDecorator<S extends Source> extends InputParameter<S> implements AnnotatedParameter<S> {

    private final CommandParameter<S> parameter;
    private final ParameterElement element;

    AnnotationParameterDecorator(CommandParameter<S> parameter, ParameterElement element) {
        super(
            parameter.name(), parameter.wrappedType(), parameter.permission(),
            parameter.description(), parameter.isOptional(),
            parameter.isFlag(), parameter.isGreedy(),
            parameter.getDefaultValueSupplier(), parameter.getSuggestionResolver()
        );
        this.parameter = parameter;
        this.element = element;
    }

    public static <S extends Source> AnnotationParameterDecorator<S> decorate(
        CommandParameter<S> parameter,
        ParameterElement element
    ) {
        return new AnnotationParameterDecorator<>(parameter, element);
    }

    /**
     * Get the instance of specific annotation
     *
     * @param clazz the type of annotation
     * @return the specific instance of an annotation of a certain type {@linkplain A}
     */
    @Override
    public <A extends Annotation> @Nullable A getAnnotation(Class<A> clazz) {
        return element.getAnnotation(clazz);
    }

    /**
     * @return the annotations associated with this parameter
     */
    @Override
    @Unmodifiable
    public Collection<? extends Annotation> getAnnotations() {
        return List.of(element.getAnnotations());
    }

    /**
     * Formats the usage parameter*
     *
     * @return the formatted parameter
     */
    @Override
    public String format() {
        return parameter.format();
    }

    /**
     * Casts the parameter to a flag parameter
     *
     * @return the parameter as a flag
     */
    @Override
    public FlagParameter<S> asFlagParameter() {
        return parameter.asFlagParameter();
    }

}
