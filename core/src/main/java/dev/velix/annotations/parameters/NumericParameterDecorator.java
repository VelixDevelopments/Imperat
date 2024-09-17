package dev.velix.annotations.parameters;

import dev.velix.command.parameters.CommandParameter;
import dev.velix.command.parameters.InputParameter;
import dev.velix.command.parameters.NumericParameter;
import dev.velix.command.parameters.NumericRange;
import org.jetbrains.annotations.Nullable;

public final class NumericParameterDecorator extends InputParameter implements NumericParameter {
    
    private final CommandParameter parameter;
    private final NumericRange range;
    
    NumericParameterDecorator(CommandParameter parameter, NumericRange range) {
        super(
                parameter.name(), parameter.wrappedType(), parameter.permission(),
                parameter.description(), parameter.isOptional(), parameter.isFlag(),
                parameter.isFlag(), parameter.getDefaultValueSupplier(),
                parameter.getSuggestionResolver()
        );
        this.parameter = parameter;
        this.range = range;
    }
    
    public static NumericParameterDecorator decorate(CommandParameter parameter, NumericRange range) {
        return new NumericParameterDecorator(parameter, range);
    }
    
    /**
     * Formats the usage parameter
     * using the command
     *
     * @return the formatted parameter
     */
    @Override
    public String format() {
        return parameter.format();
    }
    
    /**
     * @return The actual range of the numeric parameter
     * returns null if no range is specified!
     */
    @Override
    public @Nullable NumericRange getRange() {
        return range;
    }
    
}
