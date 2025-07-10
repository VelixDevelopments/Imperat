package dev.velix.imperat.context.internal;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.command.parameters.OptionalValueSupplier;
import dev.velix.imperat.command.parameters.type.ParameterTypes;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.FlagData;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.UnknownFlagException;
import dev.velix.imperat.exception.parse.UnknownSubCommandException;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.Patterns;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@SuppressWarnings("unchecked")
final class SmartUsageResolve<S extends Source> {

    private final CommandUsage<S> usage;
    private final ResolvedContext<S> context;
    private final CommandInputStream<S> stream;
    private Command<S> command;

    SmartUsageResolve(Command<S> command, ResolvedContext<S> context, CommandUsage<S> usage) {
        this.command = command;
        this.context = context;
        this.usage = usage;
        this.stream = new CommandInputStreamImpl<>(context.arguments(), usage);
    }

    public static <S extends Source> SmartUsageResolve<S> create(
        Command<S> command,
        ResolvedContext<S> context,
        CommandUsage<S> usage
    ) {
        return new SmartUsageResolve<>(command, context, usage);
    }

    private void handleEmptyOptional(CommandParameter<S> optionalEmptyParameter, CommandInputStream<S> stream) throws ImperatException {
        if (optionalEmptyParameter.isFlag()) {
            FlagParameter<S> flagParameter = optionalEmptyParameter.asFlagParameter();
            FlagData<S> flag = flagParameter.flagData();
            Object value = null;
            if (flag.isSwitch())
                value = false;
            else {

                String defaultStrValue =  flagParameter.getDefaultValueSupplier()
                        .supply(context.source(), flagParameter);
                if(defaultStrValue != null) {
                    value = flag.inputType().resolve(context, CommandInputStream.subStream(stream, defaultStrValue), defaultStrValue);
                }

            }

            ImperatDebugger.debug("Resolving empty optional FLAG parameter '%s', with value='%s'", optionalEmptyParameter.format(), value);

            context.resolveFlag(flag, null, null, value);
        } else {
            ImperatDebugger.debug("Resolving empty optional param '%s'", optionalEmptyParameter.format());
            context.resolveArgument(command, null, stream.cursor()
                .parameter, optionalEmptyParameter, getDefaultValue(context, stream, optionalEmptyParameter));
        }
    }

    public void resolve() throws ImperatException {

        final int lengthWithoutFlags = usage.getParametersWithoutFlags().size();
        ImperatDebugger.debug("Resolving input '%s'",  stream.getRawQueue().toString());
        ImperatDebugger.debug("Resolving input stream of usage '%s'", CommandUsage.format(this.command.name(), usage));
        while (stream.hasNextParameter()) {

            CommandParameter<S> currentParameter = stream.currentParameter().orElse(null);
            if (currentParameter == null) {
                ImperatDebugger.debug("Something weird, there are no params !");
                break;
            }

            String currentRaw = stream.currentRaw().orElse(null);
            if (currentRaw == null) {
                ImperatDebugger.debug("Input is too short or empty, checking for remaining optional parameters...");
                if (currentParameter.isOptional()) {
                    ImperatDebugger.debug("Found optional parameter '%s'", currentParameter.format());
                    handleEmptyOptional(currentParameter, stream);
                }
                do {
                    var param = stream.popParameter()
                            .filter(CommandParameter::isOptional)
                            .orElse(null);
                    if (param == null) break;
                    ImperatDebugger.debug("Handling other optional parameter '%s'", param.format());
                    handleEmptyOptional(param, stream);
                }while (stream.hasNextParameter());

                break;
            }else {
                ImperatDebugger.debug("Current raw is '%s', current param is '%s'", currentRaw, currentParameter.format());
            }

            if (currentParameter.isCommand()) {
                Command<S> parameterSubCmd = (Command<S>) currentParameter;
                if (parameterSubCmd.hasName(currentRaw)) this.command = parameterSubCmd;
                else throw new UnknownSubCommandException(currentRaw);
                stream.skip();
                continue;
            }

            if (Patterns.isInputFlag(currentRaw)) {
                ImperatDebugger.debug("Resolving input flag");
                if(context.hasResolvedFlag(currentParameter)) {
                    ImperatDebugger.debug("Flag-Parameter has been already resolved before!");
                    currentParameter = stream.popParameter().orElse(null);
                    if(currentParameter == null) continue;
                }

                Set<FlagData<S>> extracted = usage.getFlagExtractor().extract(Patterns.withoutFlagSign(currentRaw));
                long numberOfSwitches = extracted.stream().filter(FlagData::isSwitch)
                        .count();

                long numberOfTrueFlags = extracted.size()-numberOfSwitches;

                if(extracted.size() != numberOfSwitches && extracted.size() != numberOfTrueFlags) {
                    //we don't support flag shorthands for a mixture of true and switch flags.
                    throw new RuntimeException("Unsupported use of a mixutre of switches and true flags !");
                }

                if(extracted.size() == numberOfTrueFlags && !TypeUtility.areTrueFlagsOfSameInputTpe(extracted)) {
                    //all are true flags
                    //check if they share same type
                    throw new IllegalStateException("You cannot use compressed true flags with, while they are not of same input type");
                }

                //we are sure they all are of same-type
                for(FlagData<S> extractedFlagData : extracted) {
                    if(context.hasResolvedFlag(extractedFlagData)) {
                        ImperatDebugger.debug("Already resolved flag '%s', skipping...", extractedFlagData.name());
                        continue;
                    }

                    if(currentParameter.isFlag() && !currentParameter.asFlagParameter().flagData().equals(extractedFlagData)) {
                        ImperatDebugger.debug("Current flag parameter '%s' does not match the entered flag '%s' in the same position !",
                                currentParameter.asFlagParameter().flagData().name(), extractedFlagData.name());
                        ImperatDebugger.debug("Resolving parameter %s's default value", currentParameter.format());
                        resolveFlagDefaultValue(stream, currentParameter.asFlagParameter());
                        //no raw input , skipping a flag's parameter
                        break;
                    }

                    ImperatDebugger.debug("Resolving flag '%s'", extractedFlagData.name());
                    context.resolveFlag(ParameterTypes.flag(extractedFlagData).resolve(context, stream, currentRaw));
                }

                stream.skip();
                continue;
            }else if(currentParameter.isFlag()) {
                ImperatDebugger.debug("Current parameter is a flag while the raw '%s' doesn't match a flag input !", currentRaw);
                var nextParam = stream.peekParameter().orElse(null);

                if(nextParam == null) {
                    handleUnknownFlag(currentRaw);
                    return;
                }else if(!context.hasResolvedFlag(currentParameter)) {
                    //required
                    resolveFlagDefaultValue(stream, currentParameter.asFlagParameter());
                }
                //no raw input , skipping a flag's parameter
                stream.skipParameter();
                continue;
            }

            //ImperatDebugger.debug("FLAG DETECTED=`%s`, current-raw=`%s`, current-param=`%s`", (flag == null ? null : flag.name()), currentRaw, currentParameter.name());
            ImperatDebugger.debug("type for param '%s' is '%s'", currentParameter.format(), currentParameter.type().getClass().getTypeName());
            var value = currentParameter.type().resolve(context, stream, stream.readInput());
            ImperatDebugger.debug("AfterResolve >> current-raw=`%s`, current-param=`%s`, resolved-value='%s'", currentRaw, currentParameter.name(), value);

            if (value instanceof ExtractedInputFlag extractedInputFlag) {
                context.resolveFlag(extractedInputFlag);
                stream.skip();
            } else if (currentParameter.isOptional()) {
                ImperatDebugger.debug("Resolving optional %s, with current raw '%s'", currentParameter.name(), currentRaw);
                resolveOptional(
                    currentRaw,
                    currentParameter,
                    lengthWithoutFlags,
                    value
                );
            } else {
                //required
                ImperatDebugger.debug("Resolving required %s, with current raw '%s'", currentParameter.name(), currentRaw);
                resolveRequired(currentRaw, currentParameter, value);
            }


        }

        var lastParam = usage.getParameter(usage.size() - 1);
        while (stream.hasNextRaw()) {

            if (lastParam != null && lastParam.isGreedy()) {
                break;
            }

            String currentRaw = stream.currentRaw().orElse(null);
            if (currentRaw == null) {
                break;
            }

            var freeFlagData = command.getFlagFromRaw(currentRaw);
            if (Patterns.isInputFlag(currentRaw) && freeFlagData.isPresent()) {
                FlagData<S> freeFlag = freeFlagData.get();
                var value = ParameterTypes.flag(freeFlag).resolveFreeFlag(context, stream, freeFlag);
                context.resolveFlag(value);

            }
            stream.skipRaw();
        }
    }

    private void resolveFlagDefaultValue(CommandInputStream<S> stream, FlagParameter<S> flagParameter) throws ImperatException {
        FlagData<S> flagDataFromRaw = flagParameter.asFlagParameter().flagData();

        if(flagDataFromRaw.isSwitch()) {
            ImperatDebugger.debug("Resolving default value for switch '%s'", flagDataFromRaw.name());
            context.resolveFlag(new ExtractedInputFlag(flagDataFromRaw, null, "false", false));
            return;
        }

        //true flag default value handling
        String defValue = flagParameter.getDefaultValueSupplier().supply(context.source(), flagParameter);
        if(defValue != null) {
            Object flagValueResolved = flagParameter.getDefaultValueSupplier().isEmpty() ? null :
                    flagDataFromRaw.inputType().resolve(
                            context,
                            CommandInputStream.subStream(stream, defValue),
                            defValue
                    );
            ImperatDebugger.debug("Resolving flag '%s' default input value='%s'", flagDataFromRaw.name(), defValue);
            context.resolveFlag(new ExtractedInputFlag(flagDataFromRaw,null, defValue, flagValueResolved));
        }

    }

    private void handleUnknownFlag(String currentRaw) throws ImperatException {
        if (stream.peekParameter().isEmpty()) {
            // Last parameter - unknown flag
            throw new UnknownFlagException(currentRaw);
        } else {
            // Skip parameter for unknown flag
            ImperatDebugger.debug("Skipping param for flag raw '%s'", currentRaw);
            stream.skipParameter();
        }
    }

    private void resolveRequired(
        String currentRaw,
        CommandParameter<S> currentParameter,
        Object resolveResult
    ) throws ImperatException {
        //ImperatDebugger.visualize("Resolving required param '%s' with value '%s'", currentParameter.format(), resolveResult);
        context.resolveArgument(
            command,
            currentRaw,
            stream.currentParameterPosition(),
            currentParameter,
            resolveResult
        );
        stream.skip();
    }

    private void resolveOptional(
        String currentRaw,
        CommandParameter<S> currentParameter,
        int lengthWithoutFlags,
        Object resolveResult
    ) throws ImperatException {
        int currentParameterPosition = stream.currentParameterPosition();

        if (lengthWithoutFlags > stream.rawsLength() ) {
            int diff = lengthWithoutFlags - stream.rawsLength();

            boolean isLastParameter = stream.cursor().isLast(ShiftTarget.PARAMETER_ONLY);
            if (!isLastParameter) {
                //[o1]

                if (diff > 1) {

                    CommandParameter<S> nextParam = stream.peekParameter().filter(CommandParameter::isRequired).orElse(null);
                    if (nextParam != null) {
                        //required NEXT parameter
                        context.resolveArgument(
                                command,
                                currentRaw,
                                currentParameterPosition,
                                currentParameter,
                                getDefaultValue(context, stream, currentParameter)
                        );
                        
                        context.resolveArgument(
                                command, currentRaw,
                                currentParameterPosition + 1,
                                nextParam, resolveResult
                        );
                        
                        stream.skipParameter();
                    }else {
                        // optional next parameter, let's resolve this current parameter normally
                        context.resolveArgument(
                                command, currentRaw,
                                currentParameterPosition,
                                currentParameter,
                                resolveResult
                        );
                        stream.skip();
                    }

                } else {
                    //diff == 1
                    context.resolveArgument(
                        command,
                        currentRaw,
                        currentParameterPosition,
                        currentParameter,
                        resolveResult
                    );
                    stream.skip();
                }

            } else {

                context.resolveArgument(
                    command,
                    currentRaw,
                    currentParameterPosition,
                    currentParameter,
                    getDefaultValue(context, stream, currentParameter)
                );

                //shifting the parameters && raw again, so it can start after the new shift
                stream.skipParameter();
            }
            return;
        }

        //raw.size >= parameterSize
        context.resolveArgument(
            command, currentRaw,
            currentParameterPosition,
            currentParameter,
            resolveResult
        );
        stream.skip();
    }

    private @Nullable <T> T getDefaultValue(ExecutionContext<S> context, CommandInputStream<S> stream, CommandParameter<S> parameter) throws ImperatException {
        OptionalValueSupplier optionalSupplier = parameter.getDefaultValueSupplier();
        if(optionalSupplier.isEmpty()) {
            ImperatDebugger.debug("no def value for param='%s'", parameter.format());
            return null;
        }
        String value = optionalSupplier.supply(context.source(), parameter);

        if(value != null) {
            ImperatDebugger.debug("DEF VALUE='%s', for param='%s'", value, parameter.format());
            return (T) parameter.type().resolve(context, stream, value);
        }

        return null;
    }

    public Command<S> getCommand() {
        return command;
    }
}