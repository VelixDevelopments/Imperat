package dev.velix.imperat.command.parameters.types;

import java.lang.reflect.Type;

public interface ParameterType {
	
	Type type();
	
	boolean matchesInput(String input);
	
}
