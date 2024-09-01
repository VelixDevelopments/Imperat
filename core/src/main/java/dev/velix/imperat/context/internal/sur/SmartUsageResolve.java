package dev.velix.imperat.context.internal.sur;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.*;
import dev.velix.imperat.exceptions.CommandException;
import dev.velix.imperat.exceptions.TokenParseException;
import dev.velix.imperat.exceptions.context.ContextResolveException;
import dev.velix.imperat.resolvers.ValueResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class SmartUsageResolve<C> {
    @Getter
    private final Command<C> mainCommand;

    @Getter
    private Command<C> command;

    private final CommandUsage<C> usage;

    private final Cursor cursor = new Cursor(0, 0);

    SmartUsageResolve(Command<C> command,
                      CommandUsage<C> usage) {

        this.mainCommand = command;
        this.command = command;
        this.usage = usage;
    }

    public static <C> SmartUsageResolve<C> create(
            Command<C> command,
            CommandUsage<C> usage
    ) {
        return new SmartUsageResolve<>(command, usage);
    }

    @SuppressWarnings("unchecked")
    public void resolve(Imperat<C> dispatcher, ResolvedContext<C> context) throws CommandException {

        final List<CommandParameter> parameterList = new ArrayList<>(usage.getParameters());
        final ArgumentQueue raws = context.getArguments().copy();

        int lengthWithoutFlags = (int) usage.getParameters()
                .stream().filter((param) -> !param.isFlag())
                .count();

        while (cursor.canContinue(ShiftTarget.PARAMETER_ONLY, parameterList, raws)) {
            CommandParameter currentParameter = cursor.peekParameter(parameterList);
            assert currentParameter != null;

            String currentRaw = cursor.peekRaw(raws);
            //CommandDebugger.debug("Current raw= '%s' at %s" , currentRaw, position.raw);
            if (currentRaw == null) {
                //CommandDebugger.debug("Filling empty optional args");
                for (int i = cursor.parameter; i < parameterList.size(); i++) {
                    final CommandParameter optionalEmptyParameter = cursor.peekParameter(parameterList);
                    assert optionalEmptyParameter != null;
                    //debug("Parameter at %s = %s", i, parameter.format(command));
                    if (!optionalEmptyParameter.isOptional()) {
                        //cannot happen if no bugs, but just in case
                        throw new ContextResolveException(String.format(
                                "Missing required parameters to be filled '%s'", optionalEmptyParameter.format())
                        );
                    }

                    //all parameters from here must be optional
                    //adding the absent optional args with their default values

                    if (optionalEmptyParameter.isFlag()) {
                        CommandFlag flag = optionalEmptyParameter.asFlagParameter().getFlag();
                        Object value = null;
                        if (flag instanceof CommandSwitch) value = false;
                        else if (optionalEmptyParameter.asFlagParameter().getDefaultValueSupplier() != null) {
                            value = optionalEmptyParameter.asFlagParameter()
                                    .getDefaultValueSupplier().supply((Context<Object>) context);
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

            CommandFlag flag = usage.getFlagFromRaw(currentRaw);
            if (flag != null && currentParameter.isFlag()) {
                //CommandDebugger.debug("Found flag raw '%s' at %s", currentRaw, position.raw);
                //shifting raw only
                //check if it's switch
                if (flag instanceof CommandSwitch) {
                    //input-value is true because the flag is present
                    context.resolveFlag(currentRaw, null, true, flag);
                } else {
                    //shifting again to get the expected value
                    cursor.shift(ShiftTarget.RAW_ONLY, ShiftOperation.RIGHT);
                    String flagValueInput = cursor.peekRaw(raws);
                    Object flagDefaultValue = getDefaultValue(context, currentParameter);
                    if (flagValueInput == null) {

                        if (flagDefaultValue == null)
                            throw new ContextResolveException(String.format(
                                    "Missing required flag value-input to be filled '%s'", flag.format())
                            );

                        context.resolveFlag(currentRaw, null, flagDefaultValue, flag);
                        cursor.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
                        continue;
                    }

                    ValueResolver<C, ?> valueResolver = dispatcher.getValueResolver(flag.inputType());
                    if (valueResolver == null) {
                        throw new ContextResolveException("Cannot find resolver for flag with input type '" + flag.name() + "'");
                    }
                    context.resolveFlag(
                            currentRaw,
                            flagValueInput,
                            getResult(valueResolver, context, flagValueInput, currentParameter),
                            flag
                    );
                }

                cursor.shift(ShiftTarget.ALL, ShiftOperation.RIGHT);
                continue;
            } else if (flag == null && currentParameter.isFlag()) {
                assert currentParameter.isFlag();

                context.resolveFlag(
                        null,
                        null,
                        getDefaultValue(context, currentParameter),
                        currentParameter.asFlagParameter().getFlag()
                );

                cursor.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
                continue;
            }


            if (currentParameter.isCommand()) {

                //debug("Found command %s at %s", currentParameter.getName(), position.parameter);

                @SuppressWarnings("unchecked")
                Command<C> parameterSubCmd = (Command<C>) currentParameter;
                if (parameterSubCmd.hasName(currentRaw)) {
                    this.command = parameterSubCmd;
                } else {
                    throw new ContextResolveException("Unknown sub-command '" + currentRaw + "'");
                }

                cursor.shift(ShiftTarget.ALL, ShiftOperation.RIGHT);
                continue;
            }

            //argument input
            ValueResolver<C, ?> resolver = dispatcher.getValueResolver(currentParameter);
            if (resolver == null)
                throw new ContextResolveException("Cannot find resolver for type '" + currentParameter.getType().getName() + "'");

            if (currentParameter.isOptional()) {
                //debug("Optional parameter '%s' at position %s", currentParameter.getName(), position.parameter);
                //debug("raws-size= %s, usageMaxWithoutFlags= %s", raws.size() , (lengthWithoutFlags));
                //optional argument handling
                resolveOptional(context, resolver, raws,
                        parameterList, currentRaw, currentParameter,
                        lengthWithoutFlags);

            } else {
                //debug("Required parameter '%s' at position %s", currentParameter.getName(), position.parameter);
                resolveRequired(context, resolver,
                        raws, currentRaw, currentParameter);
            }

        }

    }

    private void resolveRequired(
            ResolvedContext<C> context,
            ValueResolver<C, ?> resolver,
            ArgumentQueue raws,
            String currentRaw,
            CommandParameter currentParameter
    ) throws CommandException {
        Object resolveResult;
        if (currentParameter.isGreedy()) {

            StringBuilder builder = new StringBuilder();
            for (int i = cursor.raw; i < raws.size(); i++) {
                builder.append(cursor.peekRaw(raws)).append(' ');
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
        //CommandDebugger.debug("Resolving required param '%s' with value '%s'", currentParameter.format(), resolveResult);
        context.resolveArgument(command, currentRaw, cursor.parameter,
                currentParameter, resolveResult);
    }

    private void resolveOptional(
            ResolvedContext<C> context,
            ValueResolver<C, ?> resolver,
            ArgumentQueue raws,
            List<CommandParameter> parameterList,
            String currentRaw,
            CommandParameter currentParameter,
            int lengthWithoutFlags
    ) throws CommandException {
        if (raws.size() < lengthWithoutFlags) {
            int diff = lengthWithoutFlags - raws.size();

            Object resolveResult = getResult(resolver, context, currentRaw, currentParameter);

            if (!cursor.isLast(ShiftTarget.PARAMETER_ONLY, parameterList, raws)) {

                if (diff > 1) {
                    CommandParameter nextParam = getNextParam(cursor.parameter + 1, parameterList, (param) -> !param.isOptional());
                    if (nextParam == null) {
                        cursor.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
                        return;
                    }
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

        Object resolveResult;
        if (currentParameter.isGreedy()) {

            StringBuilder builder = new StringBuilder();
            for (int i = cursor.raw; i < raws.size(); i++) {
                builder.append(cursor.peekRaw(raws)).append(' ');
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

    private <T> T getResult(ValueResolver<C, T> resolver, Context<C> context, String raw, CommandParameter currentParameter) throws CommandException {
        return resolver.resolve(context.getSource(), context, raw, cursor, currentParameter);
    }


    private @Nullable CommandParameter getNextParam(int start, List<CommandParameter> parameters,
                                                    Predicate<CommandParameter> parameterCondition) {
        if (start >= parameters.size()) return null;
        for (int i = start; i < parameters.size(); i++) {
            if (parameterCondition.test(parameters.get(i)))
                return parameters.get(i);

        }
        return null;
    }

    private @Nullable <T> T getDefaultValue(Context<C> context, CommandParameter parameter) {
        OptionalValueSupplier<T> optionalSupplier = parameter.getDefaultValueSupplier();
        T defaultValue = null;
        if (optionalSupplier != null) {
            defaultValue = optionalSupplier.supply(context);
        }
        return defaultValue;
    }

}
