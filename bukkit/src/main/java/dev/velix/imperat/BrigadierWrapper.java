package dev.velix.imperat;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.NumericRange;
import dev.velix.imperat.command.suggestions.CompletionArg;
import dev.velix.imperat.commodore.Commodore;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.util.TypeUtility;

import java.util.List;

// Buggy shit, needs fixing as soon as possible
//TODO fix wrong place of argument auto-completion
//LOL, EVEN MY AUTO-COMPLETION ALGORITHM IS BETTER
public final class BrigadierWrapper<C> {
	
	Commodore commodore;
	CommandDispatcher<C> dispatcher;
	
	public BrigadierWrapper(Commodore commodore, CommandDispatcher<C> dispatcher) {
		this.commodore = commodore;
		this.dispatcher = dispatcher;
	}
	
	public <T> LiteralCommandNode<T> wrapCommandToNode(Command<C> command) {
		LiteralArgumentBuilder<T> nodeBuilder = LiteralArgumentBuilder.literal(command.getName());
		
		CommandUsage<C> mainUsage = command.getMainUsage();
		this.convertMainUsage(command, nodeBuilder, mainUsage);
		LiteralCommandNode<T> node = nodeBuilder.build();
		
		for(Command<C> sub : command.getSubCommands()) {
			node.addChild(wrapCommandToNode(sub));
		}
		return node;
	}
	
	
	public <T> void convertMainUsage(Command<C> command,
	                         LiteralArgumentBuilder<T> nodeBuilder,
	                         CommandUsage<C> usage) {
		
		ArgumentBuilder<T, ?> argumentBuilder = null;
		
		int size = usage.getMaxLength();
		if(size == 0) return;
		
		for (int i = 0; i < size; i++) {
			CommandParameter parameter = usage.getParameters().get(i);
			System.out.println("Checking param : " + parameter.getName() + " for cmd " + command.getName());
			if(parameter.isCommand()) continue;
			
			//boolean isLastParam = i == (size-1);
			
			CommandDebugger.debug("Passed param %s for command %s", parameter.getName(), command.getName());
			ArgumentBuilder<T, ?> convertedArgBuilder = convertArgument(command, parameter);
			
			if(argumentBuilder == null) {
				argumentBuilder = convertedArgBuilder;
			}else {
				argumentBuilder.then(convertedArgBuilder);
			}
			
		}
		nodeBuilder.then(argumentBuilder);
	}
	
	private <T> ArgumentBuilder<T, ?> convertArgument(
					Command<C> command,
					CommandParameter parameter
	) {
		
			RequiredArgumentBuilder<T, ?> builder = RequiredArgumentBuilder.argument(parameter.getName(), from(parameter));
			builder.suggests((context, suggestions)-> {
				extractParameterSuggestions(command, context, suggestions, parameter);
				return suggestions.buildFuture();
			});
			return builder;
			
	}
	
	@SuppressWarnings("unchecked")
	private void extractParameterSuggestions(
					Command<C> command,
					CommandContext<?> context,
					SuggestionsBuilder builder,
					CommandParameter parameter
	) {
		var resolver = dispatcher.getParameterSuggestionResolver(parameter);
		if(resolver == null) return;
		//ArgumentQueue queue = ArgumentQueue.parse(context.getInput().split(" "));
		
		String input = context.getInput();
		if(input.startsWith("/")) {
			input = input.substring(1);
		}
		
		ArgumentQueue queue = ArgumentQueue.parse(input.split(" "));
		CompletionArg completionArg = new CompletionArg(queue.getLast(), queue.size()-1);
		
		List<String> results = resolver.autoComplete(command,
						(C) commodore.wrapNMSCommandSource(context.getSource()),
						queue, parameter,  completionArg);
		//TODO add description for each parameter
		System.out.println("RESULTS SIZE= " + results.size());
		results.forEach((result)-> builder.suggest(result, new LiteralMessage(parameter.getName())));
	}
	
	private ArgumentType<?> from(CommandParameter parameter) {
		Class<?> type = parameter.getType();
		if(parameter.isNumeric()) {
			NumericRange range = parameter.asNumeric().getRange();
			if (range == null)
				return numeric(type);
			else {
				return numeric(type, range);
			}
		}else if(TypeUtility.matches(parameter.getType(), boolean.class)){
			return BoolArgumentType.bool();
		}
		else {
			if(parameter.isGreedy()) {
				return StringArgumentType.greedyString();
			}else {
				return StringArgumentType.string();
			}
		}
		
	}
	
	public static ArgumentType<? extends Number> numeric(Class<?> type) {
		if(TypeUtility.matches(type, int.class)) {
			return IntegerArgumentType.integer();
		} else if (TypeUtility.matches(type, long.class)) {
			return LongArgumentType.longArg();
		} else if (TypeUtility.matches(type, float.class)) {
			return FloatArgumentType.floatArg();
		} else if (TypeUtility.matches(type, double.class)) {
			return DoubleArgumentType.doubleArg();
		} else {
			throw new IllegalArgumentException("Unsupported numeric type: " + type);
		}
	}
	
	public static ArgumentType<? extends Number> numeric(Class<?> type, NumericRange range) {
		if(TypeUtility.matches(type, int.class)) {
			return IntegerArgumentType.integer((int) range.getMin(), (int) range.getMax());
		} else if (TypeUtility.matches(type, long.class)) {
			return LongArgumentType.longArg((long) range.getMin(), (long) range.getMax());
		} else if (TypeUtility.matches(type, float.class)) {
			return FloatArgumentType.floatArg((float) range.getMin(), (float) range.getMax());
		} else if (TypeUtility.matches(type, double.class)) {
			return DoubleArgumentType.doubleArg(range.getMin(), range.getMax());
		} else {
			throw new IllegalArgumentException("Unsupported numeric type: " + type);
		}
	}
	
	
	
}
