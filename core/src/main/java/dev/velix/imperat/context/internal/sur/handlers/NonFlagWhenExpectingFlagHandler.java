package dev.velix.imperat.context.internal.sur.handlers;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.context.FlagData;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.context.internal.ExtractedInputFlag;
import dev.velix.imperat.context.internal.sur.HandleResult;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.UnknownFlagException;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.Patterns;

public final class NonFlagWhenExpectingFlagHandler<S extends Source> implements ParameterHandler<S> {
    
    @Override
    public HandleResult handle(ResolvedContext<S> context, CommandInputStream<S> stream) {
        CommandParameter<S> currentParameter = stream.currentParameter().orElse(null);
        String currentRaw = stream.currentRaw().orElse(null);
        
        if (currentParameter == null || currentRaw == null || !currentParameter.isFlag() || Patterns.isInputFlag(currentRaw)) {
            return HandleResult.NEXT_HANDLER;
        }
        
        try {
            var nextParam = stream.peekParameter().orElse(null);
            
            if (nextParam == null) {
                return HandleResult.failure(new UnknownFlagException(currentRaw));
            } else if (!context.hasResolvedFlag(currentParameter)) {
                resolveFlagDefaultValue(stream, currentParameter.asFlagParameter(), context);
            }
            
            stream.skipParameter();
            return HandleResult.NEXT_ITERATION;
        } catch (Exception e) {
            return HandleResult.failure(new ImperatException("Error handling non-flag input when expecting flag", e));
        }
    }
    
    private void resolveFlagDefaultValue(CommandInputStream<S> stream, FlagParameter<S> flagParameter, ResolvedContext<S> context) throws ImperatException {
        FlagData<S> flagDataFromRaw = flagParameter.flagData();

        if (flagDataFromRaw.isSwitch()) {
            context.resolveFlag(new ExtractedInputFlag(flagDataFromRaw, null, "false", false));
            return;
        }

        String defValue = flagParameter.getDefaultValueSupplier().supply(context.source(), flagParameter);
        if (defValue != null) {
            Object flagValueResolved = flagParameter.getDefaultValueSupplier().isEmpty() ? null :
                    flagDataFromRaw.inputType().resolve(
                            context,
                            CommandInputStream.subStream(stream, defValue),
                            defValue
                    );
            context.resolveFlag(new ExtractedInputFlag(flagDataFromRaw, null, defValue, flagValueResolved));
        }
    }
}