package dev.velix.imperat.annotations.parameters;

import dev.velix.imperat.annotations.element.ParameterCommandElement;
import dev.velix.imperat.command.parameters.InputParameter;
import dev.velix.imperat.resolvers.OptionalValueSupplier;
import org.jetbrains.annotations.Nullable;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

public abstract class AnnotatedInputParameter
				extends InputParameter implements AnnotatedParameter {
	
	private final ParameterCommandElement element;
	
	AnnotatedInputParameter(String name, Class<?> type, boolean optional,
	                        boolean flag, boolean greedy,
	                        @Nullable OptionalValueSupplier<?, ?> optionalValueSupplier,
	                        ParameterCommandElement element) {
		super(name, type, optional, flag, greedy, optionalValueSupplier);
		this.element = element;
	}
	
	@Override
	public <A extends Annotation> @Nullable A getAnnotation(Class<A> clazz) {
		return element.getAnnotation(clazz);
	}
	
	@Override
	public Collection<? extends Annotation> getAnnotations() {
		return List.of(element.getAnnotations());
	}
	
}
