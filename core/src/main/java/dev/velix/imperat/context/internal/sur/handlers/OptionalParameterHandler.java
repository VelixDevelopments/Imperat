package dev.velix.imperat.context.internal.sur.handlers;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.OptionalValueSupplier;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.context.internal.ExtractedInputFlag;
import dev.velix.imperat.context.internal.ShiftTarget;
import dev.velix.imperat.context.internal.sur.HandleResult;
import dev.velix.imperat.exception.ImperatException;
import org.jetbrains.annotations.NotNull;

public final class OptionalParameterHandler<S extends Source> implements ParameterHandler<S> {
    
    @Override
    public @NotNull HandleResult handle(ResolvedContext<S> context, CommandInputStream<S> stream) {
        CommandParameter<S> currentParameter = stream.currentParameterFast();
        String currentRaw = stream.currentRawFast();
        
        if (currentParameter == null || currentRaw == null || !currentParameter.isOptional()) {
            return HandleResult.NEXT_HANDLER;
        }
        
        try {
            var value = currentParameter.type().resolve(context, stream, stream.readInput());
            
            if (value instanceof ExtractedInputFlag extractedInputFlag) {
                context.resolveFlag(extractedInputFlag);
                stream.skip();
            } else {
                resolveOptional(currentRaw, currentParameter, context, stream, value);
            }
            
            return HandleResult.NEXT_ITERATION;
        } catch (ImperatException e) {
            return HandleResult.failure(e);
        }
    }
    
    private void resolveOptional(String currentRaw, CommandParameter<S> currentParameter, ResolvedContext<S> context,
                               CommandInputStream<S> stream, Object resolveResult) throws ImperatException {
        int currentParameterPosition = stream.currentParameterPosition();
        int lengthWithoutFlags = context.getDetectedUsage().getParametersWithoutFlags().size();

        if (lengthWithoutFlags > stream.rawsLength()) {
            int diff = lengthWithoutFlags - stream.rawsLength();
            boolean isLastParameter = stream.position().isLast(ShiftTarget.PARAMETER_ONLY);
            if (!isLastParameter) {
                if (diff > 1) {
                    CommandParameter<S> nextParam = stream.peekParameter().filter(CommandParameter::isRequired).orElse(null);
                    if (nextParam != null) {
                        context.resolveArgument(
                                context.getLastUsedCommand(),
                                currentRaw,
                                currentParameterPosition,
                                currentParameter,
                                getDefaultValue(context, stream, currentParameter)
                        );
                        
                        context.resolveArgument(
                                context.getLastUsedCommand(), currentRaw,
                                currentParameterPosition + 1,
                                nextParam, resolveResult
                        );
                        
                        stream.skipParameter();
                    } else {
                        context.resolveArgument(
                                context.getLastUsedCommand(), currentRaw,
                                currentParameterPosition,
                                currentParameter,
                                resolveResult
                        );
                        stream.skip();
                    }
                } else {
                    CommandParameter<S> nextOptionalArg = stream.peekParameter().filter(CommandParameter::isOptional).orElse(null);
                    if (nextOptionalArg != null) {
                        var n = currentParameter;
                        while (n != null) {
                            if (n.type().matchesInput(currentRaw, n)) {
                                context.resolveArgument(
                                        context.getLastUsedCommand(),
                                        currentRaw,
                                        n.position(),
                                        n,
                                        n.type().resolve(context, stream, currentRaw)
                                );
                                stream.skip();
                                break;
                            }
                            
                            Object def = getDefaultValue(context, stream, n);
                            context.resolveArgument(context.getLastUsedCommand(), n.getDefaultValueSupplier().isEmpty() ? null : n.getDefaultValueSupplier().supply(context.source(), n), n.position(), n, def);
                            
                            n = stream.popParameter().filter(CommandParameter::isOptional).orElse(null);
                        }
                        
                        if (n == null || n.isRequired()) {
                            stream.skipRaw();
                        }
                    } else {
                        context.resolveArgument(
                                context.getLastUsedCommand(),
                                currentRaw,
                                currentParameterPosition,
                                currentParameter,
                                resolveResult
                        );
                        stream.skip();
                    }
                }
            } else {
                context.resolveArgument(
                    context.getLastUsedCommand(),
                    currentRaw,
                    currentParameterPosition,
                    currentParameter,
                    getDefaultValue(context, stream, currentParameter)
                );
                stream.skipParameter();
            }
            return;
        }
        
        context.resolveArgument(
            context.getLastUsedCommand(), currentRaw,
            currentParameterPosition,
            currentParameter,
            resolveResult
        );
        stream.skip();
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getDefaultValue(ResolvedContext<S> context, CommandInputStream<S> stream, CommandParameter<S> parameter) throws ImperatException {
        OptionalValueSupplier optionalSupplier = parameter.getDefaultValueSupplier();
        if (optionalSupplier.isEmpty()) {
            return null;
        }
        String value = optionalSupplier.supply(context.source(), parameter);

        if (value != null) {
            return (T) parameter.type().resolve(context, stream, value);
        }

        return null;
    }
}