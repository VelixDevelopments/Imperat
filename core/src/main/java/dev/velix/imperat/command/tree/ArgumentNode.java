package dev.velix.imperat.command.tree;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.types.ParameterType;
import dev.velix.imperat.command.parameters.types.ParameterTypes;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.Patterns;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class ArgumentNode<S extends Source> extends ParameterNode<S, CommandParameter<S>> {

    ArgumentNode(@NotNull CommandParameter<S> data) {
        super(data);
    }

    @Override
    public boolean matchesInput(String input) {
        if (data.isFlag()) {
            int subStringIndex;
            if (Patterns.SINGLE_FLAG.matcher(input).matches()) {
                subStringIndex = 1;
            } else if (Patterns.DOUBLE_FLAG.matcher(input).matches()) {
                subStringIndex = 2;
            } else {
                subStringIndex = 0;
            }
            String flagInput = input.substring(subStringIndex);
            return data.asFlagParameter()
                .flagData().acceptsInput(flagInput);
        }
        ParameterType type = ParameterTypes.getParamType(data.type());
        return type == null || type.matchesInput(input);
    }

    @Override
    public String format() {
        return data.format();
    }

    @Override
    public int priority() {
        return 1;
    }


}
