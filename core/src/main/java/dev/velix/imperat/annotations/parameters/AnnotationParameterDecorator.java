package dev.velix.imperat.annotations.parameters;

import dev.velix.imperat.annotations.element.MethodParameterElement;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.command.parameters.InputParameter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

public final class AnnotationParameterDecorator extends InputParameter implements AnnotatedParameter {

    private final CommandParameter parameter;
    private final MethodParameterElement element;

    AnnotationParameterDecorator(CommandParameter parameter, MethodParameterElement element) {
        super(parameter.getName(), parameter.getType(), parameter.isOptional(),
                parameter.isFlag(), parameter.isGreedy(), parameter.getDefaultValueSupplier());
        this.parameter = parameter;
        this.element = element;
    }

    public static AnnotationParameterDecorator decorate(
            CommandParameter parameter,
            MethodParameterElement element
    ) {
        return new AnnotationParameterDecorator(parameter, element);
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
     * Formats the usage parameter
     * using the command
     *
     * @param command The command owning this parameter
     * @return the formatted parameter
     */
    @Override
    public <C> String format(Command<C> command) {
        return parameter.format(command);
    }

    /**
     * Casts the parameter to a flag parameter
     *
     * @return the parameter as a flag
     */
    @Override
    public FlagParameter asFlagParameter() {
        return parameter.asFlagParameter();
    }
}
