package dev.velix.imperat.brigadier;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.velix.imperat.*;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.Description;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.command.suggestions.CompletionArg;
import dev.velix.imperat.command.tree.CommandNode;
import dev.velix.imperat.command.tree.ParameterNode;
import dev.velix.imperat.commodore.Commodore;
import dev.velix.imperat.commodore.CommodoreProvider;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.TypeUtility;
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
                
                return param.type() == flagParameter.getFlagData().inputType()
                        ? argumentTypeResolver.resolveArgType(param) : null;
            }
            return TypeUtility.matches(param.type(), type) ? argumentTypeResolver.resolveArgType(param) : null;
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
        var tree = command.tree();
        var root = tree.getRoot();
        //ImperatDebugger.visualize("Parsing %s '%s'", (command.isSubCommand() ? "sub-command" : "command"), command.getName());
        return convertRoot(root);
    }
    
    private BrigadierNode convertRoot(CommandNode<BukkitSource> root) {
        BrigadierNode bRoot = BrigadierNode.create(literal(root.getData().name()));
        bRoot.withExecution(dispatcher, this)
                .withRequirement((obj) -> {
                            var source = wrapCommandSource(obj);
                            return root.getData().isIgnoringACPerms()
                                    || dispatcher.getPermissionResolver().hasPermission(source, root.getData().permission());
                        }
                );
        
        for (var child : root.getChildren()) {
            bRoot.addChild(convertNode(root, root, child));
        }
        return bRoot;
    }
    
    private BrigadierNode convertNode(CommandNode<BukkitSource> root, ParameterNode<?> parent, ParameterNode<?> node) {
        BrigadierNode child = BrigadierNode.create(argument(node.getData().name(), getArgumentType(node.getData())));
        child.withExecution(dispatcher, this)
                .withRequirement((obj) -> {
                    var permissionResolver = dispatcher.getPermissionResolver();
                    var source = wrapCommandSource(obj);
                    
                    boolean isIgnoringAC = root.getData().isIgnoringACPerms();
                    if (parent != root && parent instanceof CommandNode<?> parentCmdNode) {
                        isIgnoringAC = isIgnoringAC && parentCmdNode.getData().isIgnoringACPerms();
                    }
                    if (node instanceof CommandNode<?> commandNode) {
                        isIgnoringAC = isIgnoringAC && commandNode.getData().isIgnoringACPerms();
                    }
                    if (isIgnoringAC) {
                        return true;
                    }
                    boolean hasParentPerm = permissionResolver.hasPermission(source, parent.getData().permission());
                    boolean hasNodePerm = permissionResolver.hasPermission(source, node.getData().permission());
                    
                    return (hasParentPerm && hasNodePerm);
                });
        
        if (!(node instanceof CommandNode<?>)) {
            child.suggest(createSuggestionProvider(root.getData(), node.getData()));
        }
        
        for (var innerChild : node.getChildren()) {
            child.addChild(convertNode(root, node, innerChild));
        }
        return child;
    }

	
	/*private Predicate<Object> getParamRequirement(CommandParameter parameter) {
		return sender -> this.wrapCommandSource(sender).hasPermission(parameter.get);
	}*/
    
    private SuggestionProvider<Object> createSuggestionProvider(
            Command<BukkitSource> command,
            CommandParameter parameter
    ) {
        SuggestionResolver<BukkitSource, ?> suggestionResolver = dispatcher.getParameterSuggestionResolver(parameter);
        ImperatDebugger.debug("suggestion resolver is null=%s for param '%s'", suggestionResolver == null, parameter.format());
        if (suggestionResolver == null) {
            return ((context, builder) -> builder.buildFuture());
        }
        
        return (context, builder) -> {
            
            try {
                
                BukkitSource source = this.wrapCommandSource(context.getSource());
                String tooltipMessage = parameter.description() == Description.EMPTY ? parameter.format() : parameter.description().toString();
                Message tooltip = new LiteralMessage(tooltipMessage);
                String input = context.getInput();
                
                ArgumentQueue args = ArgumentQueue.parseAutoCompletion(
                        input.startsWith("/") ? input.substring(1) : input
                );
                
                System.out.println("ARGS=" + args);
                
                CompletionArg arg = new CompletionArg(args.getLast(), args.size() - 1);
                SuggestionContext<BukkitSource> ctx = dispatcher.getContextFactory().createSuggestionContext(dispatcher, source, command, args, arg);
                suggestionResolver
                        .autoComplete(ctx, parameter)
                        .stream()
                        .filter(c -> arg.value().isEmpty() || arg.value().isBlank() || c.toLowerCase().startsWith(arg.value().toLowerCase()))
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
