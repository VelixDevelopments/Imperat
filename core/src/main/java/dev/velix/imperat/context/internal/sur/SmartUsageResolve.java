package dev.velix.imperat.context.internal.sur;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.*;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.exception.TokenParseException;
import dev.velix.imperat.resolvers.ValueResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class SmartUsageResolve<S extends Source> {

    private final CommandUsage<S> usage;
    private final Cursor<S> cursor = new Cursor<>(0, 0);
    private Command<S> command;

    SmartUsageResolve(Command<S> command,
                      CommandUsage<S> usage) {

        this.command = command;
        this.usage = usage;
    }

    public static <S extends Source> SmartUsageResolve<S> create(
        Command<S> command,
        CommandUsage<S> usage
    ) {
        return new SmartUsageResolve<>(command, usage);
    }

    public void resolve(Imperat<S> dispatcher, ResolvedContext<S> context) throws ImperatException {
        final List<CommandParameter<S>> parameterList = new ArrayList<>(usage.getParameters());
        final ArgumentQueue raws = context.arguments().copy();

        final int lengthWithoutFlags = usage.getParametersWithoutFlags().size();

        while (cursor.canContinue(ShiftTarget.PARAMETER_ONLY, parameterList, raws)) {
            CommandParameter<S> currentParameter = cursor.peekParameter(parameterList);
            assert currentParameter != null;

            String currentRaw = cursor.peekRaw(raws);
            //ImperatDebugger.visualize("Current raw= '%s' at %s" , currentRaw, position.raw);
            if (currentRaw == null) {
                //ImperatDebugger.visualize("Filling empty optional args");
                for (int i = cursor.parameter; i < parameterList.size(); i++) {
                    final CommandParameter<S> optionalEmptyParameter = getNextParameter(parameterList);
                    //all parameters from here must be optional
                    //adding the absent optional args with their default values

                    if (optionalEmptyParameter.isFlag()) {
                        CommandFlag flag = optionalEmptyParameter.asFlagParameter().flagData();
                        Object value;
                        if (flag instanceof CommandSwitch) value = false;
                        else {
                            optionalEmptyParameter.asFlagParameter().getDefaultValueSupplier();
                            value = optionalEmptyParameter.asFlagParameter()
                                .getDefaultValueSupplier().supply(context.source());
                        }

                        context.resolveFlag(null, null, value, flag);
                    } else {
                        context.resolveArgument(command, null, cursor.parameter, optionalEmptyParameter, getDefaultValue(context, optionalEmptyParameter));
                    }
                    cursor.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
                }
                //System.out.println("Closed at position= " + position);
                break;
            }

            if (currentParameter.isCommand()) {

                //visualize("Found command %s at %s", currentParameter.getName(), position.parameter);
                Command<S> parameterSubCmd = (Command<S>) currentParameter;
                if (parameterSubCmd.hasName(currentRaw)) {
                    this.command = parameterSubCmd;
                } else {
                    throw new SourceException("Unknown sub-command '" + currentRaw + "'");
                }

                cursor.shift(ShiftTarget.ALL, ShiftOperation.RIGHT);
                continue;
            }

            CommandFlag flag = usage.getFlagFromRaw(currentRaw);
            if (flag != null && currentParameter.isFlag()) {
                //ImperatDebugger.visualize("Found flag raw '%s' at %s", currentRaw, position.raw);
                //shifting raw only
                //check if it's switch
                if (flag instanceof CommandSwitch) {
                    //input-value is true because the flag is present
                    context.resolveFlag(currentRaw, null, true, flag);
                } else {
                    // [-g <value>] <arg2>
                    //shifting again to get the expected value
                    cursor.shift(ShiftTarget.RAW_ONLY, ShiftOperation.RIGHT);
                    String flagValueRawInput = cursor.peekRaw(raws);
                    Object flagDefaultValue = getDefaultValue(context, currentParameter);
                    if (flagValueRawInput == null) {

                        if (flagDefaultValue == null) {
                            throw new SourceException(String.format(
                                "Missing required flag value-input to be filled '%s'", flag.format())
                            );
                        }

                        context.resolveFlag(currentRaw, null, flagDefaultValue, flag);
                        cursor.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
                        continue;
                    }

                    //flag value raw input is NOT NULL, resolving the value input
                    ValueResolver<S, ?> valueResolver = dispatcher.getValueResolver(flag.inputType());
                    if (valueResolver == null) {
                        throw new SourceException("Cannot find resolver for flag with input valueType '" + flag.name() + "'");
                    }
                    context.resolveFlag(
                        currentRaw,
                        flagValueRawInput,
                        getResult(valueResolver, context, flagValueRawInput, currentParameter),
                        flag
                    );
                }

                cursor.shift(ShiftTarget.ALL, ShiftOperation.RIGHT);
                continue;
            } else if (flag == null && currentParameter.isFlag()) {
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

                cursor.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
                continue;
            }

            //argument input
            ValueResolver<S, ?> resolver = dispatcher.getValueResolver(currentParameter);
            if (resolver == null)
                throw new SourceException("Cannot find resolver for valueType '" + currentParameter.valueType().getTypeName() + "'");

            if (currentParameter.isOptional()) {
                //visualize("Optional parameter '%s' at position %s", currentParameter.getName(), position.parameter);
                //visualize("raws-size= %s, usageMaxWithoutFlags= %s", raws.size() , (lengthWithoutFlags));
                //optional argument handling
                resolveOptional(context, resolver, raws,
                    parameterList, currentRaw, currentParameter,
                    lengthWithoutFlags);

            } else {
                //visualize("Required parameter '%s' at position %s", currentParameter.getName(), position.parameter);
                resolveRequired(context, resolver,
                    raws, currentRaw, currentParameter);
            }

        }

    }

    private @NotNull CommandParameter<S> getNextParameter(List<CommandParameter<S>> parameterList) throws SourceException {
        final CommandParameter<S> optionalEmptyParameter = cursor.peekParameter(parameterList);
        assert optionalEmptyParameter != null;
        //visualize("Parameter at %s = %s", i, parameter.format(command));
        if (!optionalEmptyParameter.isOptional()) {
            //cannot happen if no bugs, but just in case
            throw new SourceException("Missing required parameters to be filled '%s'", optionalEmptyParameter.format());
        }
        return optionalEmptyParameter;
    }

    private void resolveRequired(
        ResolvedContext<S> context,
        ValueResolver<S, ?> resolver,
        ArgumentQueue raws,
        String currentRaw,
        CommandParameter<S> currentParameter
    ) throws ImperatException {
        Object resolveResult;
        if (currentParameter.isGreedy()) {

            StringBuilder builder = new StringBuilder();
            final int maxRaws = raws.size();
            for (int i = cursor.raw; i < maxRaws; i++) {
                builder.append(cursor.peekRaw(raws));
                if (i != maxRaws - 1) {
                    builder.append(' ');
                }
                cursor.shift(ShiftTarget.RAW_ONLY, ShiftOperation.RIGHT);
            }

            if (builder.isEmpty()) {
                throw new TokenParseException("Failed to parse greedy argument '"
                    + currentParameter.format() + "'");
            }
            resolveResult = builder.toString();

            cursor.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
        } else {
            resolveResult = this.getResult(resolver, context, currentRaw, currentParameter);
            cursor.shift(ShiftTarget.ALL, ShiftOperation.RIGHT);
        }
        //ImperatDebugger.visualize("Resolving required param '%s' with value '%s'", currentParameter.format(), resolveResult);
        context.resolveArgument(command, currentRaw, cursor.parameter,
            currentParameter, resolveResult);
    }

    private void resolveOptional(
        ResolvedContext<S> context,
        ValueResolver<S, ?> resolver,
        ArgumentQueue raws,
        List<CommandParameter<S>> parameterList,
        String currentRaw,
        CommandParameter<S> currentParameter,
        int lengthWithoutFlags
    ) throws ImperatException {
        // /cmd <r1> [o1] <r2> [o2]
        // /cmd hi bye
        if (raws.size() < lengthWithoutFlags) {
            int diff = lengthWithoutFlags - raws.size();

            Object resolveResult = getResult(resolver, context, currentRaw, currentParameter);

            if (!cursor.isLast(ShiftTarget.PARAMETER_ONLY, parameterList, raws)) {
                //[o1]
                if (diff > 1) {
                    CommandParameter<S> nextParam = getNextParam(cursor.parameter + 1, parameterList, (param) -> !param.isOptional());
                    if (nextParam == null) {
                        //optional next parameter
                        cursor.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
                        return;
                    }

                    //required NEXT parameter
                    context.resolveArgument(command, currentRaw, cursor.parameter,
                        currentParameter, getDefaultValue(context, currentParameter));

                    context.resolveArgument(command, currentRaw, cursor.parameter + 1,
                        nextParam, resolveResult);

                    cursor.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
                } else {
                    context.resolveArgument(command, currentRaw, cursor.parameter,
                        currentParameter, resolveResult);
                    cursor.shift(ShiftTarget.ALL, ShiftOperation.RIGHT);
                }

            } else {

                context.resolveArgument(command, currentRaw, cursor.parameter,
                    currentParameter, getDefaultValue(context, currentParameter));

                //shifting the parameters && raw again, so it can start after the new shift
                cursor.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
            }
            return;
        }

        //raw.size >= parameterSize
        Object resolveResult;
        if (currentParameter.isGreedy()) {

            StringBuilder builder = new StringBuilder();
            final int maxRaws = raws.size();

            for (int i = cursor.raw; i < maxRaws; i++) {
                builder.append(cursor.peekRaw(raws));
                if (i != maxRaws - 1) {
                    builder.append(' ');
                }
                cursor.shift(ShiftTarget.RAW_ONLY, ShiftOperation.RIGHT);
            }

            if (builder.isEmpty()) {
                throw new TokenParseException("Failed to parse greedy argument '"
                    + currentParameter.format() + "'");
            }

            resolveResult = builder.toString();

            cursor.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
            context.resolveArgument(command, currentRaw, cursor.parameter,
                currentParameter, resolveResult);
        } else {
            resolveResult = getResult(resolver, context, currentRaw, currentParameter);
            context.resolveArgument(command, currentRaw, cursor.parameter, currentParameter, resolveResult);
            cursor.shift(ShiftTarget.ALL, ShiftOperation.RIGHT);
        }

    }

    private <T> T getResult(ValueResolver<S, T> resolver, ExecutionContext<S> context, String raw, CommandParameter<S> currentParameter) throws ImperatException {
        return resolver.resolve(context, currentParameter, cursor, raw);
    }


    private @Nullable CommandParameter<S> getNextParam(int start, List<CommandParameter<S>> parameters,
                                                       Predicate<CommandParameter<S>> parameterCondition) {
        if (start >= parameters.size()) return null;
        for (int i = start; i < parameters.size(); i++) {
            if (parameterCondition.test(parameters.get(i)))
                return parameters.get(i);

        }
        return null;
    }

    private @Nullable <T> T getDefaultValue(Context<S> context, CommandParameter<S> parameter) {
        OptionalValueSupplier<T> optionalSupplier = parameter.getDefaultValueSupplier();
        return optionalSupplier.supply(context.source());
    }

    public Command<S> getCommand() {
        return command;
    }
}
