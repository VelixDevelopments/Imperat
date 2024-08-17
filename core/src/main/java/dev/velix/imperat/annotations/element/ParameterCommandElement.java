package dev.velix.imperat.annotations.element;

import dev.velix.imperat.annotations.CommandAnnotationRegistry;
import lombok.Getter;

import java.lang.reflect.Parameter;

@Getter
public final class ParameterCommandElement extends CommandAnnotatedElement<Parameter> {
	
	private final String name;
	
	public ParameterCommandElement(CommandAnnotationRegistry registry,
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
