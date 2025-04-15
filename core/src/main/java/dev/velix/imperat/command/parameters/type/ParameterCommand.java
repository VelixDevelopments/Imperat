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

import java.util.List;

public final class ParameterCommand<S extends Source> extends BaseParameterType<S, Command<S>> {
    private final String name;
    ParameterCommand(String name, List<String> aliases) {
        super(new TypeWrap<>() {
        });
        this.name = name;
        suggestions.add(name);
        suggestions.addAll(aliases);
    }

    @Override
    public @Nullable Command<S> resolve(@NotNull ExecutionContext<S> context, @NotNull CommandInputStream<S> commandInputStream, String input) throws ImperatException {
        return commandInputStream.currentParameter()
            .map(CommandParameter::asCommand).orElse(null);
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<S> parameter) {
        return parameter.isCommand() &&
            parameter.asCommand().hasName(input.toLowerCase());
    }

    public String getName() {
        return name;
    }

}
