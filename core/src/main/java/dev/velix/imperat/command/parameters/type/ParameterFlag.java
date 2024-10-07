package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandFlag;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.Patterns;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParameterFlag<S extends Source> extends BaseParameterType<S, CommandFlag> {

    protected ParameterFlag() {
        super(TypeWrap.of(CommandFlag.class));
    }

    @Override
    public @Nullable CommandFlag resolve(ExecutionContext<S> context, @NotNull CommandInputStream<S> commandInputStream) throws ImperatException {
        CommandParameter<S> currentParameter = commandInputStream.currentParameter();
        assert currentParameter != null;
        if (!currentParameter.isFlag()) {
            throw new IllegalArgumentException();
        }

        FlagParameter<S> flagParameter = currentParameter.asFlagParameter();

        String rawFlag = commandInputStream.currentRaw();

        String rawInput = null;
        Object input = null;

        if (!flagParameter.isSwitch()) {
            ParameterType<S, ?> inputType = flagParameter.flagData().inputType();
            rawInput = commandInputStream.popRaw().orElseThrow();
            input = inputType.resolve(context, commandInputStream);
        }
        return new CommandFlag(flagParameter.flagData(), rawFlag, rawInput, input);
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<S> parameter) {
        int subStringIndex;
        if (Patterns.SINGLE_FLAG.matcher(input).matches()) {
            subStringIndex = 1;
        } else if (Patterns.DOUBLE_FLAG.matcher(input).matches()) {
            subStringIndex = 2;
        } else {
            subStringIndex = 0;
        }
        String flagInput = input.substring(subStringIndex);
        return parameter.asFlagParameter().flagData()
            .acceptsInput(flagInput);
    }
}
