package dev.velix.imperat.annotations.element;

import dev.velix.imperat.annotations.AnnotationRegistry;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Parameter;

@ApiStatus.Internal
@Getter
public final class ParameterCommandElement extends CommandAnnotatedElement<Parameter> {
	
	private final String name;
	
	public ParameterCommandElement(AnnotationRegistry registry,
	                               String parameterName,
	                               Parameter element) {
		super(registry, element);
		this.name = parameterName;
	}
	
	@Override
	public String toString() {
		return getElement().getType().getSimpleName() + " " + name;
	}
}
