package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ParameterCommand<S extends Source> extends BaseParameterType<S, Command<S>> {
    ParameterCommand() {
        super(new TypeWrap<>() {
        });
    }

    @Override
    public @Nullable Command<S> resolve(ExecutionContext<S> context, @NotNull CommandInputStream<S> commandInputStream) throws ImperatException {
        var currentParameter = commandInputStream.currentParameter();
        if (currentParameter == null)
            return null;
        return currentParameter.asCommand();
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<S> parameter) {
        return parameter.isCommand() &&
            parameter.asCommand().hasName(input.toLowerCase());
    }

}
