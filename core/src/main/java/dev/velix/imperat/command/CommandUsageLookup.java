package dev.velix.imperat.command;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Context;
import lombok.Data;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@ApiStatus.Internal
public final class CommandUsageLookup<C> {
	
	private final CommandDispatcher<C> dispatcher;
	private final Command<C> primeCommand;
	
	CommandUsageLookup(CommandDispatcher<C> dispatcher,
	                   Command<C> command) {
		this.dispatcher = dispatcher;
		this.primeCommand = command;
	}
	
	
	public SearchResult searchUsage(Context<C> context) {
		for (CommandUsage<C> commandUsage : primeCommand.getUsages()) {
			if (usageMatchesContext(context, commandUsage))
				return new SearchResult(commandUsage, Result.FOUND_COMPLETE);
			else if (commandUsage.hasParamType(Command.class) && checkResolvedLogic(context, commandUsage))
				return new SearchResult(commandUsage, Result.FOUND_INCOMPLETE);
		}
		
		return new SearchResult(null, Result.NOT_FOUND);
	}
	
	public List<CommandUsage<C>> findUsages(Predicate<CommandUsage<C>> predicate) {
		List<CommandUsage<C>> usages = new ArrayList<>();
		for (CommandUsage<C> usage : primeCommand.getUsages()) {
			if (predicate.test(usage)) {
				usages.add(usage);
			}
		}
		return usages;
	}
	
	private boolean usageMatchesContext(Context<C> context, CommandUsage<C> usage) {
		//1-arguments length check from both sides (raw and resolved)
		//2- compare raw and resolved parameters
		return checkLength(context.getArguments(), usage) && checkResolvedLogic(context, usage);
	}
	
	@SuppressWarnings("unchecked")
	private boolean checkResolvedLogic(Context<C> context,
	                                   CommandUsage<C> usage) {
		
		ArgumentQueue rawArgs = context.getArguments().copy();
		List<CommandParameter> parameters = usage.getParameters();
		
		int i = 0;
		while (!rawArgs.isEmpty()) {
			if (i >= parameters.size()) break;
			
			final String raw = rawArgs.poll();
			final CommandParameter parameter = parameters.get(i);
			
			if (parameter.isFlag())
				continue;
			
			if (parameter.isCommand()) {
				//the raw is the commandName
				Command<C> sub = (Command<C>) parameter;
				if (!sub.hasName(raw)) {
					return false;
				}
				
			}
			
			i++;
		}
		
		return true;
	}
	
	private boolean checkLength(ArgumentQueue rawArgs, CommandUsage<C> usage) {
		int rawLength = rawArgs.size();
		
		int maxExpectedLength = usage.getMaxLength();
		int minExpectedLength = usage.getMinLength();
		
		CommandParameter lastParameter = usage.getParameters().get(maxExpectedLength - 1);
		if (lastParameter.isGreedy()) {
			final int minMaxDiff = maxExpectedLength - minExpectedLength;
			int paramPos = lastParameter.getPosition() - minMaxDiff;
			rawLength = rawLength - (rawLength - paramPos - 1);
		}
		
		if (rawLength < minExpectedLength) {
			for (var param : usage.getParameters()) {
				if (param.isOptional()) continue;
				if (rawLength == minExpectedLength) break;
				
				if (dispatcher.getContextResolver(param) != null)
					rawLength++;
			}
		}
		
		return rawLength >= minExpectedLength && rawLength <= maxExpectedLength;
		
	}
	
	@Data
	public final class SearchResult {
		private final CommandUsage<C> commandUsage;
		private final Result result;
	}
	
	public enum Result {
		
		NOT_FOUND,
		
		FOUND_INCOMPLETE,
		
		FOUND_COMPLETE
		
	}
}
