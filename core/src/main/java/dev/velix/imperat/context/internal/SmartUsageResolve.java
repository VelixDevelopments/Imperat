package dev.velix.imperat.context.internal;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.FlagData;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import org.jetbrains.annotations.Nullable;

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

    private void handleEmptyOptional(CommandParameter<S> optionalEmptyParameter) throws ImperatException {
        if (optionalEmptyParameter.isFlag()) {
            FlagParameter<S> flagParameter = optionalEmptyParameter.asFlagParameter();
            FlagData<S> flag = flagParameter.flagData();
            Object value;
            if (flag.isSwitch())
                value = false;
            else {
                value = flagParameter.getDefaultValueSupplier()
                    .supply(context.source());
            }

            context.resolveFlag(flag, null, null, value);
        } else {
            context.resolveArgument(command, null, stream.cursor()
                .parameter, optionalEmptyParameter, getDefaultValue(context, optionalEmptyParameter));
        }
    }

    public void resolve() throws ImperatException {

        final int lengthWithoutFlags = usage.getParametersWithoutFlags().size();
        while (stream.hasNextParameter()) {

            CommandParameter<S> currentParameter = stream.currentParameter();
            assert currentParameter != null;

            String currentRaw = stream.currentRaw();
            if (currentRaw == null) {
                //ImperatDebugger.visualize("Filling empty optional args");
                if (currentParameter.isOptional()) {
                    handleEmptyOptional(currentParameter);
                }
                while (stream.hasNextParameter()) {
                    var param = stream.popParameter()
                        .filter(CommandParameter::isOptional)
                        .orElse(null);
                    if (param == null) break;
                    handleEmptyOptional(
                        param
                    );
                }
                //System.out.println("Closed at position= " + position);
                break;
            }

            if (currentParameter.isCommand()) {
                Command<S> parameterSubCmd = (Command<S>) currentParameter;
                if (parameterSubCmd.hasName(currentRaw)) this.command = parameterSubCmd;
                else throw new SourceException("Unknown sub-command '" + currentRaw + "'");
                stream.skip();
                continue;
            }

            FlagData<S> flag = usage.getFlagFromRaw(currentRaw);
            if (flag == null && currentParameter.isFlag()) {
                assert currentParameter.isFlag();
                //non identified
                //TODO write Free-flags logic
                //TODO check if it's free flag

                context.resolveFlag(
                    null,
                    null,
                    getDefaultValue(context, currentParameter),
                    currentParameter.asFlagParameter().flagData()
                );
                stream.skipParameter();
                continue;
            }
            //TODO fix the infinity error
            var value = currentParameter.type().resolve(context, stream);
            if (value instanceof CommandFlag commandFlag) {
                context.resolveFlag(commandFlag);
                stream.skip();
            } else if (currentParameter.isOptional()) {
                resolveOptional(
                    currentRaw,
                    currentParameter,
                    lengthWithoutFlags,
                    value
                );
            } else {
                //required
                resolveRequired(currentRaw, currentParameter, value);
            }


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
                        getDefaultValue(context, currentParameter)
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
                    getDefaultValue(context, currentParameter)
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

    private @Nullable <T> T getDefaultValue(Context<S> context, CommandParameter<S> parameter) {
        OptionalValueSupplier<T> optionalSupplier = parameter.getDefaultValueSupplier();
        return optionalSupplier.supply(context.source());
    }

    public Command<S> getCommand() {
        return command;
    }
}
