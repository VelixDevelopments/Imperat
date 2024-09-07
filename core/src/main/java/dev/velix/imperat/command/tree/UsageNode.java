package dev.velix.imperat.command.tree;

import dev.velix.imperat.command.parameters.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.function.Predicate;

public abstract class UsageNode<T extends CommandParameter> {

    protected final @NotNull T data;

    private final PriorityQueue<UsageNode<?>> nextNodes = new PriorityQueue<>(
            Comparator.comparing(UsageNode::priority)
    );

    protected UsageNode(@NotNull T data) {
        this.data = data;
    }

    @NotNull
    public T getData() {
        return data;
    }

    public void addChild(UsageNode<?> node) {
        if (nextNodes.contains(node)) return;
        nextNodes.add(node);
    }

    public Iterable<? extends UsageNode<?>> getChildren() {
        return nextNodes;
    }

    public abstract boolean matchesInput(String input);
    
    public abstract String format();

    public boolean isLeaf() {
        return nextNodes.isEmpty();
    }

    public abstract int priority();

    public boolean isGreedyParam() {
        return (this instanceof ArgumentNode param) && param.data.isGreedy();
    }

    public boolean isOptional() {
        return (this instanceof ArgumentNode param) && param.data.isOptional();
    }

    public @Nullable UsageNode<?> getChild(Predicate<UsageNode<?>> predicate) {
        for (var child : getChildren()) {
            if (predicate.test(child)) {
                return child;
            }
        }
        return null;
    }


    public UsageNode<?> getNextCommandChild() {
        return getChild((child) -> child instanceof CommandNode<?>);
    }

    public UsageNode<?> getNextParameterChild() {
        return getChild((child) -> child instanceof CommandNode<?>);
    }

}
