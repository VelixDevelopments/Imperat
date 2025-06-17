package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.InvalidUUIDException;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class ParameterUUID<S extends Source> extends BaseParameterType<S, UUID> {
    public ParameterUUID() {
        super();
    }

    @Override
    public @NotNull UUID resolve(
            @NotNull ExecutionContext<S> context,
            @NotNull CommandInputStream<S> commandInputStream,
            String input) throws ImperatException {

        try {
            return UUID.fromString(input);
        } catch (Exception ex) {
            throw new InvalidUUIDException(input);
        }
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<S> parameter) {
        try {
            UUID.fromString(input);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
