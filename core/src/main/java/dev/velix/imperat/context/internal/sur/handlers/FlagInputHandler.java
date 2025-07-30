package dev.velix.imperat.context.internal.sur.handlers;

import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.command.parameters.type.ParameterTypes;
import dev.velix.imperat.context.FlagData;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.context.internal.ExtractedInputFlag;
import dev.velix.imperat.context.internal.sur.HandleResult;
import dev.velix.imperat.exception.ShortHandFlagException;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.Patterns;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class FlagInputHandler<S extends Source> implements ParameterHandler<S> {
    
    @Override
    public @NotNull HandleResult handle(ResolvedContext<S> context, CommandInputStream<S> stream) {
        CommandParameter<S> currentParameter = stream.currentParameterFast();
        String currentRaw = stream.currentRawFast();
        
        if (currentParameter == null || currentRaw == null || !Patterns.isInputFlag(currentRaw)) {
            return HandleResult.NEXT_HANDLER;
        }
        
        try {
            if (context.hasResolvedFlag(currentParameter)) {
                currentParameter = stream.popParameter().orElse(null);
                if (currentParameter == null) return HandleResult.NEXT_ITERATION;
            }
            
            CommandUsage<S> usage = context.getDetectedUsage();
            Set<FlagData<S>> extracted = usage.getFlagExtractor()
                .extract(Patterns.withoutFlagSign(currentRaw));
            
            long numberOfSwitches = extracted.stream().filter(FlagData::isSwitch).count();
            long numberOfTrueFlags = extracted.size() - numberOfSwitches;
            
            if (extracted.size() != numberOfSwitches && extracted.size() != numberOfTrueFlags) {
                return HandleResult.failure(new ShortHandFlagException("Unsupported use of a mixture of switches and true flags!"));
            }
            
            if (extracted.size() == numberOfTrueFlags && !TypeUtility.areTrueFlagsOfSameInputTpe(extracted)) {
                return HandleResult.failure(new ShortHandFlagException("You cannot use compressed true-flags, while they are not of same input type"));
            }
            for (FlagData<S> extractedFlagData : extracted) {
                if (context.hasResolvedFlag(extractedFlagData)) {
                    continue;
                }
                
                if (currentParameter.isFlag() && !currentParameter.asFlagParameter().flagData().equals(extractedFlagData)) {
                    resolveFlagDefaultValue(stream, currentParameter.asFlagParameter(), context);
                    break;
                }
                context.resolveFlag(ParameterTypes.flag(extractedFlagData).resolve(context, stream, currentRaw));
            }
            
            stream.skip();
            return HandleResult.NEXT_ITERATION;
        } catch (ImperatException e) {
            return HandleResult.failure(e);
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