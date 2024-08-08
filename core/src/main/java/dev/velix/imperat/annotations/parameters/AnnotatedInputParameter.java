package dev.velix.imperat.annotations.parameters;

import dev.velix.imperat.command.parameters.InputParameter;
import dev.velix.imperat.util.AnnotationMap;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collection;

public abstract class AnnotatedInputParameter
		  extends InputParameter implements AnnotatedParameter {

	private final AnnotationMap map;
	AnnotatedInputParameter(String name, Class<?> type, boolean optional,
	                                boolean flag, boolean greedy, Object defaultValue,
	                                AnnotationMap map) {
		super(name, type, optional, flag, greedy, defaultValue);
		this.map = map;
	}

	@Override @SuppressWarnings("unchecked")
	public <A extends Annotation> @Nullable A getAnnotation(Class<A> clazz) {
		return (A) map.get(clazz);
	}

	@Override
	public Collection<? extends Annotation> getAnnotations() {
		return map.values();
	}


}
