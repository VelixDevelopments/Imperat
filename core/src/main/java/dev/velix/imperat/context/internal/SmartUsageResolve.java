package dev.velix.imperat.context.internal;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.*;
import dev.velix.imperat.exceptions.CommandException;
import dev.velix.imperat.exceptions.TokenParseException;
import dev.velix.imperat.exceptions.context.ContextResolveException;
import dev.velix.imperat.resolvers.OptionalValueSupplier;
import dev.velix.imperat.resolvers.ValueResolver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

@ApiStatus.Internal
final class SmartUsageResolve<C> {
	
	@Getter
	private final Command<C> mainCommand;
	
	@Getter
	private Command<C> command;
	
	private final CommandUsage<C> usage;
	
	private final Position position = new Position(0, 0);
	
	SmartUsageResolve(Command<C> command,
	                  CommandUsage<C> usage) {
		
		this.mainCommand = command;
		this.command = command;
		this.usage = usage;
	}
	
	static <C> SmartUsageResolve<C> create(
					Command<C> command,
					CommandUsage<C> usage
	) {
		return new SmartUsageResolve<>(command, usage);
	}
	
	@SuppressWarnings("unchecked")
	void resolve(CommandDispatcher<C> dispatcher, ResolvedContext<C> context) throws CommandException {
		
		final List<CommandParameter> parameterList = new ArrayList<>(usage.getParameters());
		final ArgumentQueue raws = context.getArguments().copy();
		
		int lengthWithoutFlags = (int) usage.getParameters()
						.stream().filter((param) -> !param.isFlag())
						.count();
		
		while (position.canContinue(ShiftTarget.PARAMETER_ONLY, parameterList, raws)) {
			CommandParameter currentParameter = position.peekParameter(parameterList);
			assert currentParameter != null;
			
			String currentRaw = position.peekRaw(raws);
			//CommandDebugger.debug("Current raw= '%s' at %s" , currentRaw, position.raw);
			if (currentRaw == null) {
				
				
				//CommandDebugger.debug("Filling empty optional args");
				for (int i = position.parameter; i < parameterList.size(); i++) {
					final CommandParameter optionalEmptyParameter = position.peekParameter(parameterList);
					assert optionalEmptyParameter != null;
					//debug("Parameter at %s = %s", i, parameter.format(command));
					if (!optionalEmptyParameter.isOptional()) {
						//cannot happen if no bugs, but just in case
						throw new ContextResolveException(String.format(
										"Missing required parameters to be filled '%s'", optionalEmptyParameter.format(command))
						);
					}
					
					//all parameters from here must be optional
					//adding the absent optional args with their default values
					
					if(optionalEmptyParameter.isFlag()) {
						CommandFlag flag = optionalEmptyParameter.asFlagParameter().getFlag();
						Object value = null;
						if (flag instanceof CommandSwitch) value = false;
						else if(optionalEmptyParameter.asFlagParameter().getDefaultValueSupplier() != null){
							value = optionalEmptyParameter.asFlagParameter()
											.getDefaultValueSupplier().supply((Context<Object>) context);
						}
						
						context.resolveFlag(null, null, value, flag);
					}else {
						context.resolveArgument(command, null, position.parameter, optionalEmptyParameter, getDefaultValue(context, optionalEmptyParameter));
					}
					position.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
				}
				//System.out.println("Closed at position= " + position);
				break;
			}
			
			CommandFlag flag = usage.getFlagFromRaw(currentRaw);
			if (flag != null && currentParameter.isFlag()) {
				//CommandDebugger.debug("Found flag raw '%s' at %s", currentRaw, position.raw);
				//shifting raw only
				//check if it's switch
				if(flag instanceof CommandSwitch) {
					//input-value is true because the flag is present
 					context.resolveFlag(currentRaw, null, true, flag);
				}else {
					//shifting again to get the expected value
					position.shift(ShiftTarget.RAW_ONLY, ShiftOperation.RIGHT);
					String flagValueInput = position.peekRaw(raws);
					Object flagDefaultValue = getDefaultValue(context, currentParameter);
					if(flagValueInput == null) {
						
						if(flagDefaultValue == null)
							throw new ContextResolveException(String.format(
											"Missing required flag value-input to be filled '%s'", flag.format())
							);
						
						context.resolveFlag(currentRaw, null, flagDefaultValue, flag);
						position.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
						continue;
					}
					
					ValueResolver<C, ?> valueResolver = dispatcher.getValueResolver(flag.inputType());
					if(valueResolver == null) {
						throw new ContextResolveException("Cannot find resolver for flag with input type '" + flag.name() + "'");
					}
					context.resolveFlag(
									currentRaw,
									flagValueInput,
									getResult(valueResolver, context, flagValueInput),
									flag
					);
				}
				
				position.shift(ShiftTarget.ALL, ShiftOperation.RIGHT);
				continue;
			}
			else if(flag == null && currentParameter.isFlag()) {
				assert currentParameter.isFlag();
				
				context.resolveFlag(
								null,
								null,
								getDefaultValue(context, currentParameter),
								currentParameter.asFlagParameter().getFlag()
				);
				
				position.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
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
				
				position.shift(ShiftTarget.ALL, ShiftOperation.RIGHT);
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
	
	private void resolveRequired(ResolvedContext<C> context,
	                             ValueResolver<C, ?> resolver,
	                             ArgumentQueue raws,
	                             String currentRaw,
	                             CommandParameter currentParameter) throws CommandException {
		Object resolveResult;
		if (currentParameter.isGreedy()) {
			
			StringBuilder builder = new StringBuilder();
			for (int i = position.raw; i < raws.size(); i++) {
				builder.append(position.peekRaw(raws)).append(' ');
				position.shift(ShiftTarget.RAW_ONLY, ShiftOperation.RIGHT);
			}
			
			if (builder.isEmpty()) {
				throw new TokenParseException("Failed to parse greedy argument '"
								+ currentParameter.format(command) + "'");
			}
			resolveResult = builder.toString();
			
			position.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
		} else {
			resolveResult = this.getResult(resolver, context, currentRaw);
			position.shift(ShiftTarget.ALL, ShiftOperation.RIGHT);
		}
		
		context.resolveArgument(command, currentRaw, position.parameter,
						currentParameter, resolveResult);
	}
	
	private void resolveOptional(ResolvedContext<C> context,
	                             ValueResolver<C, ?> resolver,
	                             ArgumentQueue raws,
	                             List<CommandParameter> parameterList,
	                             String currentRaw,
	                             CommandParameter currentParameter,
	                             int lengthWithoutFlags) throws CommandException {
		if (raws.size() < lengthWithoutFlags) {
			int diff = lengthWithoutFlags - raws.size();
			
			Object resolveResult = getResult(resolver, context, currentRaw);
			
			if (!position.isLast(ShiftTarget.PARAMETER_ONLY, parameterList, raws)) {
				
				if (diff > 1) {
					CommandParameter nextParam = getNextParam(position.parameter + 1, parameterList, (param) -> !param.isOptional());
					if (nextParam == null) {
						position.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
						return;
					}
					context.resolveArgument(command, currentRaw, position.parameter,
									currentParameter, getDefaultValue(context, currentParameter));
					
					context.resolveArgument(command, currentRaw, position.parameter + 1,
									nextParam, resolveResult);
					
					position.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
				} else {
					context.resolveArgument(command, currentRaw, position.parameter,
									currentParameter, resolveResult);
					position.shift(ShiftTarget.ALL, ShiftOperation.RIGHT);
				}
				
			} else {
				
				context.resolveArgument(command, currentRaw, position.parameter,
								currentParameter, getDefaultValue(context, currentParameter));
				
				//shifting the parameters && raw again, so it can start after the new shift
				position.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
			}
			return;
		}
		
		Object resolveResult;
		if (currentParameter.isGreedy()) {
			
			StringBuilder builder = new StringBuilder();
			for (int i = position.raw; i < raws.size(); i++) {
				builder.append(position.peekRaw(raws)).append(' ');
				position.shift(ShiftTarget.RAW_ONLY, ShiftOperation.RIGHT);
			}
			
			if (builder.isEmpty()) {
				throw new TokenParseException("Failed to parse greedy argument '"
								+ currentParameter.format(command) + "'");
			}
			resolveResult = builder.toString();
			
			position.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
			context.resolveArgument(command, currentRaw, position.parameter,
							currentParameter, resolveResult);
		} else {
			resolveResult = getResult(resolver, context, currentRaw);
			context.resolveArgument(command, currentRaw, position.parameter, currentParameter, resolveResult);
			position.shift(ShiftTarget.ALL, ShiftOperation.RIGHT);
		}
		
	}
	
	private <T> T getResult(ValueResolver<C, T> resolver, Context<C> context, String raw) throws CommandException {
		return resolver.resolve(context.getCommandSource(), context, raw);
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
	
	@Data
	@AllArgsConstructor
	static final class Position {
		
		private int parameter, raw;
		
		public void shift(ShiftTarget shift, IntUnaryOperator operator) {
			if (shift == ShiftTarget.RAW_ONLY)
				this.raw = operator.applyAsInt(raw);
			else if (shift == ShiftTarget.PARAMETER_ONLY)
				this.parameter = operator.applyAsInt(parameter);
			else {
				this.raw = operator.applyAsInt(raw);
				this.parameter = operator.applyAsInt(parameter);
			}
			//System.out.println("r=" + raw + ", p=" +parameter);
		}
		
		public void shift(ShiftTarget target, ShiftOperation operation) {
			shift(target, operation.operator);
		}
		
		public boolean canContinue(ShiftTarget target,
		                           List<CommandParameter> parameters,
		                           ArgumentQueue queue) {
			return target.canContinue(this, parameters.size(), queue.size());
		}
		
		public @Nullable CommandParameter peekParameter(List<CommandParameter> parameters) {
			return parameters.get(this.parameter);
		}
		
		public @Nullable String peekRaw(ArgumentQueue raws) {
			try {
				return raws.get(raw);
			} catch (Exception ex) {
				return null;
			}
		}
		
		public boolean isLast(ShiftTarget shiftTarget, int maxParams, int maxRaws) {
			if (shiftTarget == ShiftTarget.PARAMETER_ONLY)
				return parameter == maxParams - 1;
			else if (shiftTarget == ShiftTarget.RAW_ONLY)
				return raw == maxRaws - 1;
			else
				return parameter == maxParams - 1 && raw == maxRaws - 1;
		}
		
		public boolean isLast(ShiftTarget shiftTarget, List<CommandParameter> params, ArgumentQueue raws) {
			return isLast(shiftTarget, params.size(), raws.size());
		}
	}
	
	enum ShiftOperation {
		RIGHT(value -> value + 1),
		
		LEFT(value -> value - 1);
		
		private final IntUnaryOperator operator;
		
		ShiftOperation(IntUnaryOperator operator) {
			this.operator = operator;
		}
		
	}
	
	enum ShiftTarget {
		
		RAW_ONLY((pos, maxParam, maxRaw) -> pos.raw < maxRaw),
		
		PARAMETER_ONLY((pos, maxParam, maxRaw) -> pos.parameter < maxParam),
		
		ALL((pos, maxRaw, maxParameter) ->
						pos.raw < maxRaw && pos.parameter < maxParameter);
		
		private final PositionShiftCondition canContinueCheck;
		
		ShiftTarget(PositionShiftCondition canContinueCheck) {
			this.canContinueCheck = canContinueCheck;
		}
		
		boolean canContinue(Position position, int maxParam, int maxRaw) {
			return canContinueCheck.canContinue(position, maxParam, maxRaw);
		}
	}
	
	@FunctionalInterface
	interface PositionShiftCondition {
		boolean canContinue(Position position, int maxRaw, int maxParameter);
	}
	
}
