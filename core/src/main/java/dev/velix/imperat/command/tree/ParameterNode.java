package dev.velix.imperat.command.tree;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.function.Predicate;

public abstract class ParameterNode<S extends Source, T extends CommandParameter<S>> {

    protected final @NotNull T data;

    private final PriorityQueue<ParameterNode<S, ?>> nextNodes = new PriorityQueue<>(
        Comparator.comparing(ParameterNode::priority)
    );

    protected ParameterNode(@NotNull T data) {
        this.data = data;
    }

    @NotNull
    public T getData() {
        return data;
    }

    public void addChild(ParameterNode<S, ?> node) {
        if (nextNodes.contains(node)) return;
        nextNodes.add(node);
    }

    public Iterable<? extends ParameterNode<S, ?>> getChildren() {
        return nextNodes;
    }

    public abstract boolean matchesInput(String input);

    public abstract String format();

    public boolean isLast() {
        return nextNodes.isEmpty();
    }

    public abstract int priority();

    public boolean isGreedyParam() {
        return (this instanceof ArgumentNode<?> param) && param.data.isGreedy();
    }

    public boolean isOptional() {
        return (this instanceof ArgumentNode<?> param) && param.data.isOptional();
    }

    public @Nullable ParameterNode<S, ?> getChild(Predicate<ParameterNode<S, ?>> predicate) {
        for (var child : getChildren()) {
            if (predicate.test(child)) {
                return child;
            }
        }
        return null;
    }

    public ParameterNode<S, ?> getNextCommandChild() {
        return getChild((child) -> child instanceof CommandNode<?>);
    }

    public ParameterNode<S, ?> getNextParameterChild() {
        return getChild((child) -> true);
    }

    public boolean isRequired() {
        return data.isRequired();
    }

    public boolean isCommand() {
        return this instanceof CommandNode || data.isCommand();
    }
}
