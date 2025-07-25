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
import dev.velix.imperat.exception.FlagException;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.Patterns;
import dev.velix.imperat.util.TypeUtility;

import java.util.Set;

public final class FlagInputHandler<S extends Source> implements ParameterHandler<S> {
    
    @Override
    public HandleResult handle(ResolvedContext<S> context, CommandInputStream<S> stream) {
        CommandParameter<S> currentParameter = stream.currentParameter().orElse(null);
        String currentRaw = stream.currentRaw().orElse(null);
        
        if (currentParameter == null || currentRaw == null || !Patterns.isInputFlag(currentRaw)) {
            return HandleResult.CONTINUE;
        }
        
        try {
            ImperatDebugger.debug("[FlagInputHandler] Resolving input flag");
            if (context.hasResolvedFlag(currentParameter)) {
                ImperatDebugger.debug("[FlagInputHandler] Flag-Parameter has been already resolved before!");
                currentParameter = stream.popParameter().orElse(null);
                if (currentParameter == null) return HandleResult.GO_BACK;
            }
            
            CommandUsage<S> usage = context.getDetectedUsage();
            Set<FlagData<S>> extracted = usage.getFlagExtractor()
                .extract(Patterns.withoutFlagSign(currentRaw));
            
            long numberOfSwitches = extracted.stream().filter(FlagData::isSwitch).count();
            long numberOfTrueFlags = extracted.size() - numberOfSwitches;
            
            if (extracted.size() != numberOfSwitches && extracted.size() != numberOfTrueFlags) {
                return HandleResult.failure(new FlagException("Unsupported use of a mixture of switches and true flags!"));
            }
            
            if (extracted.size() == numberOfTrueFlags && !TypeUtility.areTrueFlagsOfSameInputTpe(extracted)) {
                return HandleResult.failure(new FlagException("You cannot use compressed true-flags, while they are not of same input type"));
            }
            
            for (FlagData<S> extractedFlagData : extracted) {
                if (context.hasResolvedFlag(extractedFlagData)) {
                    ImperatDebugger.debug("[FlagInputHandler] Already resolved flag '%s', skipping...", extractedFlagData.name());
                    continue;
                }
                
                if (currentParameter.isFlag() && !currentParameter.asFlagParameter().flagData().equals(extractedFlagData)) {
                    ImperatDebugger.debug("[FlagInputHandler] Current flag parameter '%s' does not match the entered flag '%s' in the same position!",
                        currentParameter.asFlagParameter().flagData().name(), extractedFlagData.name());
                    ImperatDebugger.debug("[FlagInputHandler] Resolving parameter %s's default value", currentParameter.format());
                    resolveFlagDefaultValue(stream, currentParameter.asFlagParameter(), context);
                    break;
                }
                
                ImperatDebugger.debug("[FlagInputHandler] Resolving flag '%s'", extractedFlagData.name());
                context.resolveFlag(ParameterTypes.flag(extractedFlagData).resolve(context, stream, currentRaw));
            }
            
            stream.skip();
            return HandleResult.GO_BACK;
        } catch (ImperatException e) {
            return HandleResult.failure(e);
        }
    }
    
    private void resolveFlagDefaultValue(CommandInputStream<S> stream, FlagParameter<S> flagParameter, ResolvedContext<S> context) throws ImperatException {
        FlagData<S> flagDataFromRaw = flagParameter.flagData();

        if (flagDataFromRaw.isSwitch()) {
            ImperatDebugger.debug("[FlagInputHandler] Resolving default value for switch '%s'", flagDataFromRaw.name());
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
            ImperatDebugger.debug("[FlagInputHandler] Resolving flag '%s' default input value='%s'", flagDataFromRaw.name(), defValue);
            context.resolveFlag(new ExtractedInputFlag(flagDataFromRaw, null, defValue, flagValueResolved));
        }
    }
}