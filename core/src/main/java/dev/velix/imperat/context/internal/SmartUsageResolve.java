package dev.velix.imperat.context.internal;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.UsageParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.exceptions.CommandException;
import dev.velix.imperat.exceptions.TokenParseException;
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


	void resolve(CommandDispatcher<C> dispatcher, ResolvedContext<C> context) throws CommandException {

		final List<UsageParameter> parameterList = new ArrayList<>(usage.getParameters());
		final ArgumentQueue raws = context.getArguments().copy();

		int lengthWithoutFlags = (int) usage.getParameters()
				  .stream().filter((param) -> !param.isFlag())
				  .count();

		while (position.canContinue(ShiftTarget.PARAMETER_ONLY, parameterList, raws)) {

			UsageParameter currentParameter = position.peekParameter(parameterList);
			assert currentParameter != null;
			if (currentParameter.isFlag()) {
				//debug("Found flag param '%s' at %s", currentParameter.getName(), position.parameter);
				position.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
				continue;
			}

			String currentRaw = position.peekRaw(raws);
			if (currentRaw == null) {
				for (int i = position.parameter; i < parameterList.size(); i++) {
					UsageParameter parameter = position.peekParameter(parameterList);
					assert parameter != null;
					if (!parameter.isOptional()) {
						//cannot happen if no bugs, but just in case
						throw new IllegalStateException(String.format(
								  "Missing required parameters to be filled '%s'", parameter.format(command))
						);
					}
					//all parameters from here must be optional
					//adding the absent optional args with their default values
					context.resolveArgument(command, null, position.parameter,
							  currentParameter, currentParameter.getDefaultValue());
					position.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
				}
				//System.out.println("Closed at position= " + position);
				break;
			}
			//debug("Raw= %s at %s", currentRaw, position.raw);

			if (context.getFlagExtractor().isKnownFlag(mainCommand, currentRaw)) {
				//debug("Found flag raw '%s' at %s", currentRaw, position.raw);
				position.shift(ShiftTarget.RAW_ONLY, ShiftOperation.RIGHT);
				continue;
			}

			if (currentParameter.isCommand()) {

				//debug("Found command %s at %s", currentParameter.getName(), position.parameter);

				@SuppressWarnings("unchecked")
				Command<C> parameterSubCmd = (Command<C>) currentParameter;
				if (parameterSubCmd.hasName(currentRaw)) {
					this.command = parameterSubCmd;
				} else {
					throw new IllegalArgumentException("Unknown sub-command '" + currentRaw + "'");
				}

				position.shift(ShiftTarget.ALL, ShiftOperation.RIGHT);
				continue;
			}

			//argument input
			ValueResolver<C, ?> resolver = dispatcher.getValueResolver(currentParameter);
			if (resolver == null)
				throw new IllegalStateException("Cannot find resolver for type '" + currentParameter.getType().getName() + "'");

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
	                             UsageParameter currentParameter) throws CommandException {
		Object resolveResult;
		if (currentParameter.isGreedy()) {

			StringBuilder builder = new StringBuilder();
			for (int i = position.raw; i < raws.size(); i++) {
				builder.append(position.peekRaw(raws)).append(" ");
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
	                             List<UsageParameter> parameterList,
	                             String currentRaw,
	                             UsageParameter currentParameter,
	                             int lengthWithoutFlags) throws CommandException {
		if (raws.size() < lengthWithoutFlags) {
			int diff = lengthWithoutFlags - raws.size();

			Object resolveResult = getResult(resolver, context, currentRaw);

			if (!position.isLast(ShiftTarget.PARAMETER_ONLY, parameterList, raws)) {

				if (diff > 1) {
					UsageParameter nextParam = getNextParam(position.parameter + 1, parameterList, (param) -> !param.isOptional());
					if (nextParam == null) {
						position.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
						return;
					}

					context.resolveArgument(command, currentRaw, position.parameter,
							  currentParameter, currentParameter.getDefaultValue());

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
						  currentParameter, currentParameter.getDefaultValue());

				//shifting the parameters && raw again, so it can start after the new shift
				position.shift(ShiftTarget.PARAMETER_ONLY, ShiftOperation.RIGHT);
			}
		} else {

			Object resolveResult;
			if (currentParameter.isGreedy()) {

				StringBuilder builder = new StringBuilder();
				for (int i = position.raw; i < raws.size(); i++) {
					builder.append(position.peekRaw(raws)).append(" ");
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

	}

	/*private void debug(String msg, Object... objects) {
		System.out.println(String.format(msg, objects));
	}*/

	private <T> T getResult(ValueResolver<C, T> resolver, Context<C> context, String raw) throws CommandException {
		return resolver.resolve(context.getCommandSource(), context, raw);
	}


	private @Nullable UsageParameter getNextParam(int start, List<UsageParameter> parameters,
	                                              Predicate<UsageParameter> parameterCondition) {
		if (start >= parameters.size()) return null;
		for (int i = start; i < parameters.size(); i++) {
			if (parameterCondition.test(parameters.get(i)))
				return parameters.get(i);

		}
		return null;
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
		                           List<UsageParameter> parameters,
		                           ArgumentQueue queue) {
			return target.canContinue(this, parameters.size(), queue.size());
		}

		public @Nullable UsageParameter peekParameter(List<UsageParameter> parameters) {
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

		public boolean isLast(ShiftTarget shiftTarget, List<UsageParameter> params, ArgumentQueue raws) {
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

		private final PositionContinuation canContinueCheck;

		ShiftTarget(PositionContinuation canContinueCheck) {
			this.canContinueCheck = canContinueCheck;
		}

		boolean canContinue(Position position, int maxParam, int maxRaw) {
			return canContinueCheck.canContinue(position, maxParam, maxRaw);
		}
	}

	@FunctionalInterface
	interface PositionContinuation {
		boolean canContinue(Position position, int maxRaw, int maxParameter);
	}

}
