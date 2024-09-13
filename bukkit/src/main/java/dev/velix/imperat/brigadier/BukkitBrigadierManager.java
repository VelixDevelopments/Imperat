package dev.velix.imperat.brigadier;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.velix.imperat.*;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.Description;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.command.suggestions.CompletionArg;
import dev.velix.imperat.commodore.Commodore;
import dev.velix.imperat.commodore.CommodoreProvider;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.CommandDebugger;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import static dev.velix.imperat.commodore.CommodoreProvider.isSupported;

public final class BukkitBrigadierManager implements BrigadierManager<BukkitSource> {
    
    private final BukkitImperat dispatcher;
    private final Commodore commodore;
    
    private final List<ArgumentTypeResolver> resolvers = new ArrayList<>();
    
    
    public BukkitBrigadierManager(BukkitImperat dispatcher) {
        this.dispatcher = dispatcher;
        this.commodore = CommodoreProvider.getCommodore(dispatcher.getPlatform());
        if (isSupported()) {
            registerArgumentResolver(String.class, DefaultArgTypeResolvers.STRING);
            registerArgumentResolver(DefaultArgTypeResolvers.NUMERIC);
            registerArgumentResolver(Boolean.class, DefaultArgTypeResolvers.BOOLEAN);
            registerArgumentResolver(Player.class, DefaultArgTypeResolvers.PLAYER);
            registerArgumentResolver(OfflinePlayer.class, DefaultArgTypeResolvers.PLAYER);
        }
    }
    
    public static BukkitBrigadierManager load(BukkitImperat bukkitCommandDispatcher) {
        if (!isSupported()) {
            return null;
        }
        return new BukkitBrigadierManager(bukkitCommandDispatcher);
    }
    
    
    @Override
    public Imperat<BukkitSource> getDispatcher() {
        return dispatcher;
    }
    
    @Override
    public BukkitSource wrapCommandSource(Object commandSource) {
        return dispatcher.wrapSender(commodore.wrapNMSCommandSource(commandSource));
    }
    
    @Override
    public <T> void registerArgumentResolver(
            Class<T> type,
            ArgumentTypeResolver argumentTypeResolver
    ) {
        resolvers.add((param) -> {
            if (param.isFlag()) {
                
                FlagParameter flagParameter = param.asFlagParameter();
                if (flagParameter.isSwitch()) {
                    return argumentTypeResolver.resolveArgType(flagParameter);
                }
                
                return param.getType() == flagParameter.getFlagData().inputType()
                        ? argumentTypeResolver.resolveArgType(param) : null;
            }
            return param.getType() == type ? argumentTypeResolver.resolveArgType(param) : null;
        });
    }
    
    @Override
    public void registerArgumentResolver(ArgumentTypeResolver argumentTypeResolver) {
        resolvers.add(argumentTypeResolver);
    }
    
    @Override
    public @NotNull ArgumentType<?> getArgumentType(CommandParameter parameter) {
        for (var resolver : resolvers) {
            var resolved = resolver.resolveArgType(parameter);
            if (resolved != null)
                return resolved;
        }
        return getStringArgType(parameter);
    }
    
    
    private StringArgumentType getStringArgType(CommandParameter parameter) {
        if (parameter.isGreedy()) return StringArgumentType.greedyString();
        else return StringArgumentType.string();
    }
    
    @Override
    public BrigadierNode parseCommandIntoNode(Command<BukkitSource> command) {
        BrigadierNode root = BrigadierNode.create(literal(command.getName()));
        //CommandDebugger.visualize("Parsing %s '%s'", (command.isSubCommand() ? "sub-command" : "command"), command.getName());
        //input
        CommandUsage<BukkitSource> mainUsage = command.getMainUsage();
        
        BrigadierNode last = root;
        
        for (CommandParameter parameter : mainUsage.getParameters()) {
            //we parse an actual brigadier argument
            //CommandDebugger.visualize("Attempting to add args to %s", command.getName());
            last = parseParameter(command, mainUsage, parameter);
            root.addChild(last);
        }
        
        //CommandDebugger.visualize("Trying to add children for command '%s'", command.getName());
        for (Command<BukkitSource> sub : command.getSubCommands()) {
            //CommandDebugger.visualize("Found child '%s' for parent '%s'", sub.getName(), command.getName());
            //last = parseCommand(sub, last);
            parseSubCommand(sub, last);
        }
        
        return root;
    }
    
    private void parseSubCommand(Command<BukkitSource> command, BrigadierNode lastParent) {
        BrigadierNode literalSub = BrigadierNode.create(literal(command.getName()));
        lastParent.addChild(literalSub);
        lastParent = literalSub;
        
        //CommandDebugger.visualize("Parsing %s '%s'", (command.isSubCommand() ? "sub-command" : "command"), command.getName());
        //input
        CommandUsage<BukkitSource> mainUsage = command.getMainUsage();
        //CommandDebugger.visualize("Main usage '%s'", CommandUsage.format(command, mainUsage));
        
        for (CommandParameter parameter : mainUsage.getParameters()) {
            //CommandDebugger.visualize("Attempting to add args to %s", command.getName());
            if (parameter.isCommand()) {
                continue;
            }
            BrigadierNode child = parseParameter(command, mainUsage, parameter);
            lastParent.addChild(child);
            lastParent = child;
        }
        
        //parse other inner children
		/*for(var sub : command.getSubCommands()) {
			parseSubCommand(sub, lastParent);
		}*/
    
    }
    
    
    private BrigadierNode parseParameter(Command<BukkitSource> command,
                                         CommandUsage<BukkitSource> usage,
                                         CommandParameter parameter) {
        CommandDebugger.debug("Parsing parameter '%s' for cmd '%s'", parameter.getName(), command.getName());
        CommandDebugger.debug("Entering usage '%s'", CommandUsage.format(command, usage));
        if (parameter.isFlag()) {
            CommandDebugger.debug("Found flag parameter");
            FlagParameter flagParameter = parameter.asFlagParameter();
            CommandFlag flag = flagParameter.getFlagData();
            
            //TODO find a better workaround for aliases of the flag
            var node = BrigadierNode.create(argument("-" + flag.name(), StringArgumentType.word()))
                    .suggest((context, suggestionBuilder) -> {
                        suggestionBuilder.suggest("-" + flag.name());
                        for (String alias : flag.aliases()) {
                            suggestionBuilder.suggest("-" + alias);
                        }
                        return suggestionBuilder.buildFuture();
                    });
            
            if (!flagParameter.isSwitch()) {
                var flagInputArgType = getArgumentType(flagParameter);
                BrigadierNode flagInputNode = BrigadierNode.create(argument("value", flagInputArgType));
                node.addChild(flagInputNode);
                return flagInputNode;
            }
            
            return node;
        }
        
        
        ArgumentType<?> argumentType = this.getArgumentType(parameter);
        //CommandDebugger.visualize("Found value type = " + argumentType.getClass().getSimpleName());
        //CommandDebugger.visualize("Parameter position = '%s' , with usage max= '%s'", parameter.getPosition(), usage.getMaxLength());
        
        int max = command.isSubCommand() ? usage.getMaxLength() : usage.getMaxLength() - 1;
        boolean isLast = parameter.getPosition() == max;
        
        //CommandDebugger.visualize("isLast= " + isLast);
        
        BrigadierNode node = BrigadierNode.create(argument(parameter.getName(), argumentType));
        
        //CommandDebugger.visualize("Resolving suggestions");
        node.withRequirement((sender) -> true)
                .suggest(createSuggestionProvider(command, parameter));
        
        if (isLast) {
            //CommandDebugger.visualize("Setting execution !");
            node.withExecution(dispatcher, this);
        }
        return node;
    }
	
	/*private Predicate<Object> getParamRequirement(CommandParameter parameter) {
		return sender -> this.wrapCommandSource(sender).hasPermission(parameter.get);
	}*/
    
    private SuggestionProvider<Object> createSuggestionProvider(
            Command<BukkitSource> command,
            CommandParameter parameter
    ) {
        if (parameter.getSuggestionResolver() == null)
            return null;
        
        return (context, builder) -> {
            SuggestionResolver<BukkitSource, ?> suggestionResolver = dispatcher.getParameterSuggestionResolver(parameter);
            if (suggestionResolver == null) {
                return null;
            }
            
            try {
                
                BukkitSource actor = this.wrapCommandSource(context.getSource());
                String tooltipMessage = parameter.getDescription() == Description.EMPTY ? parameter.format() : parameter.getDescription().toString();
                Message tooltip = new LiteralMessage(tooltipMessage);
                String input = context.getInput();
                
                ArgumentQueue args = ArgumentQueue.parseAutoCompletion(
                        input.startsWith("/") ? input.substring(1) : input
                );
                CompletionArg arg = new CompletionArg(args.getLast(), args.size() - 1);
                
                suggestionResolver
                        .autoComplete(command,
                                actor, args, parameter, arg
                        )
                        .stream()
                        .filter(c -> c.toLowerCase().startsWith(arg.value().toLowerCase()))
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
            Command<BukkitSource> imperatCommand
    ) {
        commodore.register(bukkitCmd,
                parseCommandIntoNode(imperatCommand).toInternalNode());
    }
}
