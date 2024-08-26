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
	
	@Override @SuppressWarnings("unchecked")
	public BrigadierNode parseCommandIntoNode(Command<CommandSender> command) {
		BrigadierNode root = BrigadierNode.create(LiteralArgumentBuilder.literal(command.getName()));
		
		//input
		CommandUsage<CommandSender> mainUsage = command.getMainUsage();
		for(CommandParameter parameter : mainUsage.getParameters()) {
			if(parameter.isFlag()) {
				//TODO deal with it later on, continue for now
				continue;
			}
			if(parameter.isCommand()) {
				root.addChild(parseCommandIntoNode((Command<CommandSender>) parameter.asCommand()));
				continue;
			}
			//else we parse an actual brigadier argument
			root.addChild(parseParameter(command, mainUsage, parameter));
		}
		
		for(Command<CommandSender> sub : command.getSubCommands()) {
			root.addChild(parseCommandIntoNode(sub));
		}
		
		return root;
	}
	
	
	private BrigadierNode parseParameter(Command<CommandSender> command, CommandUsage<CommandSender> usage,
	                                     CommandParameter parameter) {
		
		ArgumentType<?> argumentType = this.getArgumentType(parameter);
		boolean isLast = parameter.getPosition() == usage.getMaxLength() - 1;
		
		BrigadierNode node = BrigadierNode.create(argument(parameter.getName(), argumentType));
		node.withRequirement((sender)-> true)
				.suggest(createSuggestionProvider(command, parameter));
		
		if (isLast)
			node.withExecution(dispatcher, this);
		
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
				String tooltipMessage = parameter.getDescription() == Description.EMPTY ? parameter.getName() : parameter.getDescription().toString();
				Message tooltip = new LiteralMessage(tooltipMessage);
				String input = context.getInput();
				
				ArgumentQueue args = ArgumentQueue.parse(
								input.startsWith("/") ? input.substring(1) : input
				);
				CompletionArg arg = new CompletionArg(args.getLast(), args.size()-1);
				
				SuggestionResolver<CommandSender, ?> suggestionResolver = parameter.getSuggestionResolver();
				suggestionResolver
								.autoComplete(command,
												actor, args, parameter, arg
								)
								.stream()
								.filter(c -> c.toLowerCase().startsWith(args.getLast().toLowerCase()))
								.sorted(String.CASE_INSENSITIVE_ORDER)
								.distinct()
								.forEach(c -> builder.suggest(c, tooltip));
				
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
