package dev.velix.imperat;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.CommandDebugger;

import java.util.function.Predicate;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class BrigadierNode {

    private final CommandNode<?> node;


    BrigadierNode(CommandNode<?> node) {
        this.node = node;
    }

    public static BrigadierNode create(ArgumentBuilder<?, ?> argumentBuilder) {
        return new BrigadierNode(argumentBuilder.build());
    }

    public static BrigadierNode create(CommandNode<?> commandNode) {
        return new BrigadierNode(commandNode);
    }

    public BrigadierNode addChild(BrigadierNode node) {
        CommandDebugger.debug("Adding child '%s' of type='%s'", node.node.getName(), (node.node instanceof ArgumentCommandNode<?, ?> ? "ARGUMENT" : "LITERAL"));
        this.node.addChild(node.toInternalNode());
        return this;
    }

    public <S extends Source> BrigadierNode withExecution(
            Imperat<S> dispatcher,
            BrigadierManager<S> manager
    ) {
        setExecution((context) -> {
            String input = context.getInput();
            S sender = manager.wrapCommandSource(context.getSource());
            dispatcher.dispatch(sender, context.getRootNode().getName(), input);
            return Command.SINGLE_SUCCESS;
        });
        return this;
    }

    public BrigadierNode withRequirement(
            Predicate<Object> req
    ) {
        NodeModifier.setRequirement(this.node, (Predicate) req);
        return this;
    }

    public BrigadierNode suggest(SuggestionProvider provider) {
        if (!(node instanceof ArgumentCommandNode)) {
            throw new IllegalArgumentException("Not an argument node.");
        }
        NodeModifier.setSuggestionProvider(((ArgumentCommandNode) node), provider);
        return this;
    }

    public <T extends CommandNode<?>> T toInternalNode() {
        return (T) node;
    }

    private void setExecution(Command<?> brigadierCmdAction) {
        NodeModifier.setCommand(node, (Command) brigadierCmdAction);
    }

}
