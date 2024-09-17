package dev.velix.command.parameters.types;

import java.lang.reflect.Type;

public interface ParameterType {
    
    Type type();
    
    boolean matchesInput(String input);
    
}
