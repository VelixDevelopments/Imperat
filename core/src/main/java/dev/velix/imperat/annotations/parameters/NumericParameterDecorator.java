package dev.velix.imperat.annotations.parameters;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.InputParameter;
import dev.velix.imperat.command.parameters.NumericParameter;
import dev.velix.imperat.command.parameters.NumericRange;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.Nullable;

public final class NumericParameterDecorator<S extends Source> extends InputParameter<S> implements NumericParameter<S> {

    private final CommandParameter<S> parameter;
    private final NumericRange range;

    NumericParameterDecorator(CommandParameter<S> parameter, NumericRange range) {
        super(
                parameter.name(), parameter.wrappedType(), parameter.permission(),
                parameter.description(), parameter.isOptional(), parameter.isFlag(),
                parameter.isFlag(), parameter.getDefaultValueSupplier(),
                parameter.getSuggestionResolver()
        );
        this.parameter = parameter;
        this.range = range;
    }

    public static <S extends Source> NumericParameterDecorator<S> decorate(CommandParameter<S> parameter, NumericRange range) {
        return new NumericParameterDecorator<>(parameter, range);
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
