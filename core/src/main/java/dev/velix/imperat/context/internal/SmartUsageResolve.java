package dev.velix.imperat.context.internal;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.command.parameters.OptionalValueSupplier;
import dev.velix.imperat.command.parameters.type.ParameterFlag;
import dev.velix.imperat.command.parameters.type.ParameterTypes;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.FlagData;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.Patterns;
import org.jetbrains.annotations.Nullable;

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
                else throw new SourceException("Unknown sub-command '" + currentRaw + "'");
                stream.skip();
                continue;
            }

            FlagData<S> flag = usage.getFlagParameterFromRaw(currentRaw);
            if (currentParameter.isFlag()) {
                handleParameterFlag(stream, currentParameter.asFlagParameter(), currentRaw, flag);
                continue;
            } else if (Patterns.isInputFlag(currentRaw)) {

                ImperatDebugger.debug("Flag pattern of raw '%s'", currentRaw);
                if(command.getFlagFromRaw(currentRaw).isPresent()) {

                    //FOUND FREE FLAG
                    var flagData = command.getFlagFromRaw(currentRaw).get();
                    ImperatDebugger.debug("Found free flag '%s' from raw input '%s'", flagData.name(), currentRaw);

                    ParameterFlag<S> parameterFlag = ParameterTypes.flag(flagData);
                    context.resolveFlag(parameterFlag.resolveFreeFlag(context, stream, flagData));

                }else {

                    /*
                     *  if the current param IS NOT A FLAG, & the current input matches a flag's criteria
                     *  if the input also isn't from the free flags register that is linked to the command's instance, THEN
                     *  this can be a case of two consecutive optional flags next to each other , example usage:
                     *  '/ban <target> [-silent] [-ip] [duration] [reason]'
                     *  If user enters '/ban mqzen -s -ip' this should work flawlessly, while '/ban mqzen -ip -s' DOESN'T resolve the silent flag,
                     *  because of the algorithm that retains the parameters order while resolving their values.
                     *  I think this can be fixed in the command tree instead of doing it here by making the tree shuffle the possibilities of
                     *  flag-command-nodes.
                     */

                    //TODO fix this in the command tree
                }

                //we skip the raw input cursor only
                stream.skipRaw();
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

    private void handleParameterFlag(
            CommandInputStream<S> stream,
            FlagParameter<S> currentParameter,
            String currentRaw,
            @Nullable FlagData<S> flagDataFromRaw
    ) throws ImperatException {
        ImperatDebugger.debug("Found parameter flag '%s' from raw input '%s'", currentParameter.format(), currentRaw);
        ImperatDebugger.debug("It's FlagData='%s'", (flagDataFromRaw == null ? "NULL" : flagDataFromRaw.name()));

        if (flagDataFromRaw == null) {
            var nextParam = stream.peekParameter().orElse(null);

            if(nextParam == null || nextParam.isOptional()) {
                handleUnknownFlag(currentRaw);
                return;
            }else {
                //required
                resolveFlagDefaultValue(stream, currentParameter);
            }
            //no raw input , skipping a flag's parameter
            stream.skipParameter();
            return;
        }

        // flag != null
        ParameterFlag<S> parameterFlagType = (ParameterFlag<S>) currentParameter.type();
        CommandParameter<S> lookedUpFlagParameter = findMatchingFlagParameter(currentParameter, currentRaw, parameterFlagType);

        if (lookedUpFlagParameter == null) {
            throw new SourceException("Unknown flag '%s'", currentRaw);
        }

        if (!lookedUpFlagParameter.asFlagParameter().name().equals(currentParameter.name())) {
            //a flag in between got skipped, we should resolve its defaults
            //currentParameter is the skipped one
            resolveFlagDefaultValue(stream, currentParameter.asFlagParameter());

            stream.skipParameter();
        }else {
            context.resolveFlag(parameterFlagType.resolve(context, stream, stream.readInput()));
            stream.skip();
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
        Object flagValueResolved = flagParameter.getDefaultValueSupplier().isEmpty() ? null :
                flagDataFromRaw.inputType().resolve(
                    context,
                    defValue == null ? stream : CommandInputStream.subStream(stream, defValue),
                    defValue
                );
        ImperatDebugger.debug("Resolving flag '%s' default input value='%s'", flagDataFromRaw.name(), defValue);
        context.resolveFlag(new ExtractedInputFlag(flagDataFromRaw,null, defValue, flagValueResolved));

//        if(enteredFlagParameter.isFlag()) {
//            stream.skipRaw();
//        }
    }

    private void handleUnknownFlag(String currentRaw) throws SourceException {
        if (stream.peekParameter().isEmpty()) {
            // Last parameter - unknown flag
            throw new SourceException("Unknown flag '%s'", currentRaw);
        } else {
            // Skip parameter for unknown flag
            ImperatDebugger.debug("Skipping param for flag raw '%s'", currentRaw);
            stream.skipParameter();
        }
    }

    private @Nullable CommandParameter<S> findMatchingFlagParameter(
            CommandParameter<S> currentParameter,
            String currentRaw,
            ParameterFlag<S> parameterFlag
    ) {
        CommandInputStream<S> stream = this.stream.copy();
        CommandParameter<S> flagParam = currentParameter;

        do {
            if(parameterFlag.matchesInput(currentRaw, flagParam)) {
                return flagParam;
            }

            if (!flagParam.isFlag()) {
                continue;
            }
            flagParam = stream.popParameter().orElse(null);
        } while (flagParam != null);

        return null;
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
        // /cmd <r1> [o1] <r2> [o2]
        // /cmd hi bye
        int currentParameterPosition = stream.currentParameterPosition();

        if (stream.rawsLength() < lengthWithoutFlags) {
            int diff = lengthWithoutFlags - stream.rawsLength();


            if (!stream.cursor().isLast(ShiftTarget.PARAMETER_ONLY)) {
                //[o1]

                if (diff > 1) {

                    CommandParameter<S> nextParam = stream.peekParameter().filter(CommandParameter::isRequired).orElse(null);
                    if (nextParam == null) {
                        //optional next parameter
                        stream.skipParameter();
                        return;
                    }
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
                } else {
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
        ImperatDebugger.debug("DEF VALUE='%s', for param='%s'", value, parameter.format());
        return (T) parameter.type().resolve(context, stream, value);
    }

    public Command<S> getCommand() {
        return command;
    }
}