package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.FlagData;
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

    public CommandFlag resolveFreeFlag(
        ExecutionContext<S> context,
        @NotNull CommandInputStream<S> commandInputStream,
        FlagData<S> freeFlag
    ) throws ImperatException {
        String rawFlag = commandInputStream.currentRaw().orElse(null);
        if (rawFlag == null) {
            throw new IllegalArgumentException();
        }
        String rawInput = null;
        Object input = null;

        if (!freeFlag.isSwitch()) {
            ParameterType<S, ?> inputType = freeFlag.inputType();
            rawInput = commandInputStream.popRaw().orElse(null);
            if (rawInput != null) {
                input = inputType.resolve(context, commandInputStream);
            }
        } else {
            input = true;
        }
        return new CommandFlag(freeFlag, rawFlag, rawInput, input);
    }

    @Override
    public @Nullable CommandFlag resolve(ExecutionContext<S> context, @NotNull CommandInputStream<S> commandInputStream) throws ImperatException {
        var currentParameter = commandInputStream.currentParameter()
            .orElse(null);
        if (currentParameter == null)
            return null;

        if (!currentParameter.isFlag()) {
            throw new IllegalArgumentException();
        }

        FlagParameter<S> flagParameter = currentParameter.asFlagParameter();

        String rawFlag = commandInputStream.currentRaw().orElse(null);
        if (rawFlag == null) {
            throw new IllegalArgumentException();
        }

        String rawInput = null;
        Object input = null;

        if (!flagParameter.isSwitch()) {
            ParameterType<S, ?> inputType = flagParameter.flagData().inputType();
            rawInput = commandInputStream.popRaw().orElse(null);
            if (rawInput != null) {
                input = inputType.resolve(context, commandInputStream);
            }
        } else {
            input = true;
        }
        return new CommandFlag(flagParameter.flagData(), rawFlag, rawInput, input);
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<S> parameter) {
        int subStringIndex;
        if (Patterns.SINGLE_FLAG.matcher(input).matches())
            subStringIndex = 1;
        else if (Patterns.DOUBLE_FLAG.matcher(input).matches())
            subStringIndex = 2;
        else
            subStringIndex = 0;

        String flagInput = input.substring(subStringIndex);
        return parameter.asFlagParameter().flagData()
            .acceptsInput(flagInput);
    }
}
