package dev.velix.imperat.context.internal.sur.handlers;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.context.internal.ExtractedInputFlag;
import dev.velix.imperat.context.internal.sur.HandleResult;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.ImperatDebugger;

public final class RequiredParameterHandler<S extends Source> implements ParameterHandler<S> {
    
    @Override
    public HandleResult handle(ResolvedContext<S> context, CommandInputStream<S> stream) {
        CommandParameter<S> currentParameter = stream.currentParameter().orElse(null);
        String currentRaw = stream.currentRaw().orElse(null);
        
        if (currentParameter == null || currentRaw == null || currentParameter.isOptional()) {
            return HandleResult.NEXT_HANDLER;
        }
        
        try {
            ImperatDebugger.debug("[RequiredParameterHandler] type for param '%s' is '%s'", currentParameter.format(), currentParameter.type().getClass().getTypeName());
            var value = currentParameter.type().resolve(context, stream, stream.readInput());
            ImperatDebugger.debug("[RequiredParameterHandler] AfterResolve >> current-raw=`%s`, current-param=`%s`, resolved-value='%s'", currentRaw, currentParameter.name(), value);
            
            if (value instanceof ExtractedInputFlag extractedInputFlag) {
                context.resolveFlag(extractedInputFlag);
                stream.skip();
            } else {
                ImperatDebugger.debug("[RequiredParameterHandler] Resolving required %s, with current raw '%s'", currentParameter.format(), currentRaw);
                context.resolveArgument(context.getLastUsedCommand(), currentRaw, stream.currentParameterPosition(), currentParameter, value);
                stream.skip();
            }
            
            return HandleResult.NEXT_ITERATION;
        } catch (ImperatException e) {
            return HandleResult.failure(e);
        }
    }
}