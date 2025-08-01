package dev.velix.imperat.paramtypes;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.components.TestSource;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import org.jetbrains.annotations.NotNull;

public final class TestPlayerParamType extends BaseParameterType<TestSource, TestPlayer> {
    
    final static int MAX_ARGS = 5;
    @Override
    public @NotNull TestPlayer resolve(
            @NotNull ExecutionContext<TestSource> context,
            @NotNull CommandInputStream<TestSource> inputStream,
            @NotNull String input
    ) throws ImperatException {
        return new TestPlayer(input);
    }
    
    @Override
    public boolean matchesInput(String input, CommandParameter<TestSource> parameter) {
        try{
            Double.parseDouble(input);
            return false;
        }catch (Exception exception) {
            return input.length() <= 16;
        }
    }
}
