package dev.velix.imperat.context.internal.sur.handlers;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.context.internal.sur.HandleResult;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.parse.UnknownSubCommandException;

public final class CommandParameterHandler<S extends Source> implements ParameterHandler<S> {
    
    @Override
    public HandleResult handle(ResolvedContext<S> context, CommandInputStream<S> stream) {
        CommandParameter<S> currentParameter = stream.currentParameter().orElse(null);
        String currentRaw = stream.currentRaw().orElse(null);
        
        if (currentParameter == null || currentRaw == null || !currentParameter.isCommand()) {
            return HandleResult.NEXT_HANDLER;
        }
        
        try {
            Command<S> parameterSubCmd = (Command<S>) currentParameter;
            if (parameterSubCmd.hasName(currentRaw)) {
                context.setLastCommand(parameterSubCmd);
                stream.skip();
                return HandleResult.NEXT_ITERATION;
            } else {
                return HandleResult.failure(new UnknownSubCommandException(currentRaw));
            }
        } catch (Exception e) {
            return HandleResult.failure(new ImperatException("Error processing command parameter", e));
        }
    }
}