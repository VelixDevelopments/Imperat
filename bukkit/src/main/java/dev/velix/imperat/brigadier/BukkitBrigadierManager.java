package dev.velix.imperat.brigadier;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.velix.imperat.*;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.Description;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.suggestions.CompletionArg;
import dev.velix.imperat.commodore.Commodore;
import dev.velix.imperat.commodore.CommodoreProvider;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.resolvers.SuggestionResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import static dev.velix.imperat.commodore.CommodoreProvider.isSupported;

public final class BukkitBrigadierManager implements BrigadierManager<CommandSender> {
	
	private final BukkitCommandDispatcher dispatcher;
	private final Commodore commodore;
	
	private final List<ArgumentTypeResolver> resolvers = new ArrayList<>();
	
	
	public BukkitBrigadierManager(BukkitCommandDispatcher dispatcher) {
		this.dispatcher = dispatcher;
		this.commodore = CommodoreProvider.getCommodore(dispatcher.getPlatform());
		if(isSupported()) {
			registerArgumentResolver(String.class, DefaultArgTypeResolvers.STRING);
			registerArgumentResolver(DefaultArgTypeResolvers.NUMERIC);
			registerArgumentResolver(Boolean.class, DefaultArgTypeResolvers.BOOLEAN);
			registerArgumentResolver(Player.class, DefaultArgTypeResolvers.PLAYER);
			registerArgumentResolver(OfflinePlayer.class, DefaultArgTypeResolvers.PLAYER);
		}
	}
	
	public static BukkitBrigadierManager load(BukkitCommandDispatcher bukkitCommandDispatcher) {
		if(!isSupported()) {
			return null;
		}
		return new BukkitBrigadierManager(bukkitCommandDispatcher);
	}
	
	
	@Override
	public CommandDispatcher<CommandSender> getDispatcher() {
		return dispatcher;
	}
	
	@Override
	public CommandSender wrapCommandSource(Object commandSource) {
		return commodore.wrapNMSCommandSource(commandSource);
	}
	
	@Override
	public <T> void registerArgumentResolver(
					Class<T> type,
					ArgumentTypeResolver argumentTypeResolver
	) {
		resolvers.add((param)-> param.getType() == type ? argumentTypeResolver.resolveArgType(param) : null);
	}
	
	@Override
	public void registerArgumentResolver(ArgumentTypeResolver argumentTypeResolver) {
		resolvers.add(argumentTypeResolver);
	}
	
	@Override
	public @NotNull ArgumentType<?> getArgumentType(CommandParameter parameter) {
		for(var resolver : resolvers) {
			var resolved = resolver.resolveArgType(parameter);
			if(resolved != null)
				return resolved;
		}
		return getStringArgType(parameter);
	}
	

	
	private StringArgumentType getStringArgType(CommandParameter parameter) {
		if(parameter.isGreedy()) return StringArgumentType.greedyString();
		else return StringArgumentType.string();
	}
	
	@Override
	public BrigadierNode parseCommandIntoNode(Command<CommandSender> command) {
		BrigadierNode root = BrigadierNode.create(LiteralArgumentBuilder.literal(command.getName()));
		//CommandDebugger.debug("Parsing %s '%s'", (command.isSubCommand() ? "sub-command" : "command"), command.getName());
		//input
		CommandUsage<CommandSender> mainUsage = command.getMainUsage();
		
		BrigadierNode last = root;
		
		for(CommandParameter parameter : mainUsage.getParameters()) {
			if(parameter.isFlag()) {
				//TODO deal with it later on, continue for now
				continue;
			}
			//else we parse an actual brigadier argument
			//CommandDebugger.debug("Attempting to add args to %s", command.getName());
			last = parseParameter(command, mainUsage, parameter);
			root.addChild(last);
		}
		
		//CommandDebugger.debug("Trying to add children for command '%s'", command.getName());
		for(Command<CommandSender> sub : command.getSubCommands()) {
			//CommandDebugger.debug("Found child '%s' for parent '%s'", sub.getName(), command.getName());
			//last = parseCommand(sub, last);
			last.addChild(parseCommand(sub, last));
		}
		
		return root;
	}
	
	private BrigadierNode parseCommand(Command<CommandSender> command, BrigadierNode lastParent) {
		BrigadierNode literalSub = BrigadierNode.create(LiteralArgumentBuilder.literal(command.getName()));
		lastParent.addChild(literalSub);
		lastParent = literalSub;
		
		//CommandDebugger.debug("Parsing %s '%s'", (command.isSubCommand() ? "sub-command" : "command"), command.getName());
		//input
		CommandUsage<CommandSender> mainUsage = command.getMainUsage();
		//CommandDebugger.debug("Main usage '%s'", CommandUsage.format(command, mainUsage));
		
		for(CommandParameter parameter : mainUsage.getParameters()) {
			if(parameter.isFlag()) {
				//TODO deal with it later on, continue for now
				continue;
			}
			//CommandDebugger.debug("Attempting to add args to %s", command.getName());
			
			BrigadierNode child = parseParameter(command, mainUsage, parameter);
			lastParent.addChild(child);
			lastParent = child;
		}
		
		//parse other inner children
		for(var sub : command.getSubCommands()) {
			lastParent.addChild(parseCommand(sub, lastParent));
		}
		
		return lastParent;
	}
	
	
	private BrigadierNode parseParameter(Command<CommandSender> command,
	                                     CommandUsage<CommandSender> usage,
	                                     CommandParameter parameter) {
		//CommandDebugger.debug("Parsing parameter '%s' for cmd '%s'", parameter.getName() , command.getName());
		//CommandDebugger.debug("Entering usage '%s'", CommandUsage.format(command, usage));
		ArgumentType<?> argumentType = this.getArgumentType(parameter);
		//CommandDebugger.debug("Found arg type = " + argumentType.getClass().getSimpleName());
		//CommandDebugger.debug("Parameter position = '%s' , with usage max= '%s'", parameter.getPosition(), usage.getMaxLength());
		
		int max = command.isSubCommand() ? usage.getMaxLength() : usage.getMaxLength()-1;
		boolean isLast = parameter.getPosition() == max;
		
		//CommandDebugger.debug("isLast= " + isLast);
		
		BrigadierNode node = BrigadierNode.create(argument(parameter.getName(), argumentType));
		
		//CommandDebugger.debug("Resolving suggestions");
		node.withRequirement((sender)-> true)
				.suggest(createSuggestionProvider(command, parameter));
		
		if (isLast) {
			//CommandDebugger.debug("Setting execution !");
			node.withExecution(dispatcher, this);
		}
		return node;
	}
	
	/*private Predicate<Object> getParamRequirement(CommandParameter parameter) {
		return sender -> this.wrapCommandSource(sender).hasPermission(parameter.get);
	}*/
	
	private SuggestionProvider<Object> createSuggestionProvider(
					Command<CommandSender> command,
					CommandParameter parameter
	) {
		if (parameter.getSuggestionResolver() == null)
			return null;

		return (context, builder) -> {
			try {
				CommandSender actor = this.wrapCommandSource(context.getSource());
				String tooltipMessage = parameter.getDescription() == Description.EMPTY ? parameter.format() : parameter.getDescription().toString();
				Message tooltip = new LiteralMessage(tooltipMessage);
				String input = context.getInput();
				
				ArgumentQueue args = ArgumentQueue.parseAutoCompletion(
								input.startsWith("/") ? input.substring(1) : input
				);
				CompletionArg arg = new CompletionArg(args.getLast(), args.size()-1);
				
				SuggestionResolver<CommandSender, ?> suggestionResolver = parameter.getSuggestionResolver();
				suggestionResolver
								.autoComplete(command,
												actor, args, parameter, arg
								)
								.stream()
								.filter(c -> c.toLowerCase().startsWith(arg.arg().toLowerCase()))
								.distinct()
								.sorted(String.CASE_INSENSITIVE_ORDER)
								.forEach(suggestionResult -> builder.suggest(suggestionResult, tooltip));
				
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return builder.buildFuture();
		};
	}
	
	public void registerBukkitCommand(
					org.bukkit.command.Command bukkitCmd,
					Command<CommandSender> imperatCommand
	) {
		commodore.register(bukkitCmd,
						parseCommandIntoNode(imperatCommand).toInternalNode());
	}
}
