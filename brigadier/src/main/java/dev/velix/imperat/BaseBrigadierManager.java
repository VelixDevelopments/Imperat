package dev.velix.imperat;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.Description;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.command.suggestions.CompletionArg;
import dev.velix.imperat.command.tree.CommandNode;
import dev.velix.imperat.command.tree.ParameterNode;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

public abstract non-sealed class BaseBrigadierManager<S extends Source> implements BrigadierManager<S> {
    
    protected final Imperat<S> dispatcher;
    protected final List<ArgumentTypeResolver> resolvers = new ArrayList<>();
    
    protected BaseBrigadierManager(Imperat<S> dispatcher) {
        this.dispatcher = dispatcher;
    }
    
    @Override
    public <CN extends com.mojang.brigadier.tree.CommandNode<?>> @NotNull CN parseCommandIntoNode(@NotNull Command<S> command) {
        var tree = command.tree();
        var root = tree.getRoot();
        //ImperatDebugger.visualize("Parsing %s '%s'", (command.isSubCommand() ? "sub-command" : "command"), command.getName());
        return convertRoot(root).toInternalNode();
    }
    
    private BrigadierNode convertRoot(CommandNode<S> root) {
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
    
    private BrigadierNode convertNode(CommandNode<S> root, ParameterNode<?, ?> parent, ParameterNode<S, ?> node) {
        BrigadierNode child = BrigadierNode.create(node instanceof CommandNode<?> ? literal(node.getData().name()) : argument(node.getData().name(), getArgumentType(node.getData())));
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
    
    
    private @NotNull SuggestionProvider<Object> createSuggestionProvider(
            Command<S> command,
            CommandParameter<S> parameter
    ) {
        SuggestionResolver<S, ?> suggestionResolver = dispatcher.getParameterSuggestionResolver(parameter);
        ImperatDebugger.debug("suggestion resolver is null=%s for param '%s'", suggestionResolver == null, parameter.format());
        if (suggestionResolver == null) {
            String paramFormat = parameter.format();
            String desc = parameter.description() == Description.EMPTY ? parameter.description().toString() : "";
            return ((context, builder) -> builder.suggest(paramFormat, new LiteralMessage(paramFormat + (desc.isEmpty() ? "" : " - " + desc)))
                    .buildFuture());
        }
        
        return (context, builder) -> {
            
            try {
                
                S source = this.wrapCommandSource(context.getSource());
                String paramFormat = parameter.format();
                String desc = parameter.description() == Description.EMPTY ? parameter.description().toString() : "";
                Message tooltip = new LiteralMessage(paramFormat + (desc.isEmpty() ? "" : " - " + desc));
                
                String input = context.getInput();
                
                ArgumentQueue args = ArgumentQueue.parseAutoCompletion(
                        input.startsWith("/") ? input.substring(1) : input
                );
                
                CompletionArg arg = new CompletionArg(args.getLast(), args.size() - 1);
                SuggestionContext<S> ctx = dispatcher.getContextFactory().createSuggestionContext(dispatcher, source, command, args, arg);
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
    
    
    //resolvers methods
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> void registerArgumentResolver(
            Class<T> type,
            ArgumentTypeResolver argumentTypeResolver
    ) {
        resolvers.add((param) -> {
            if (param.isFlag()) {
                
                FlagParameter<S> flagParameter = (FlagParameter<S>) param.asFlagParameter();
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
    public @NotNull ArgumentType<?> getArgumentType(CommandParameter<S> parameter) {
        for (var resolver : resolvers) {
            var resolved = resolver.resolveArgType(parameter);
            if (resolved != null)
                return resolved;
        }
        return getStringArgType(parameter);
    }
    
    
    private StringArgumentType getStringArgType(CommandParameter<S> parameter) {
        if (parameter.isGreedy()) return StringArgumentType.greedyString();
        else return StringArgumentType.string();
    }
    
    
}
