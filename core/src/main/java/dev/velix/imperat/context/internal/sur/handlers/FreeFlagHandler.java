package dev.velix.imperat.context.internal.sur.handlers;

import dev.velix.imperat.command.parameters.type.ParameterTypes;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.FlagData;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.context.internal.sur.HandleResult;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.Patterns;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class FreeFlagHandler<S extends Source> implements ParameterHandler<S> {
    
    @Override
    public @NotNull HandleResult handle(ExecutionContext<S> context, CommandInputStream<S> stream) {
        var lastParam = context.getDetectedUsage().getParameter(context.getDetectedUsage().size() - 1);
        
        String currentRaw;
        while ((currentRaw = stream.currentRawFast()) != null) {
            if (lastParam != null && lastParam.isGreedy()) {
                break;
            }

            Optional<FlagData<S>> freeFlagData = context.getLastUsedCommand().getFlagFromRaw(currentRaw);
            if (Patterns.isInputFlag(currentRaw) && freeFlagData.isPresent()) {
                try {
                    FlagData<S> freeFlag = freeFlagData.get();
                    var value = ParameterTypes.flag(freeFlag).resolveFreeFlag(context, stream, freeFlag);
                    context.resolveFlag(value);
                } catch (ImperatException e) {
                    return HandleResult.failure(e);
                }
            }
            stream.skipRaw();
        }
        
        return HandleResult.TERMINATE;
    }
}