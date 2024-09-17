package dev.velix.command.tree;

import dev.velix.command.parameters.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.function.Predicate;

public abstract class ParameterNode<T extends CommandParameter> {
    
    protected final @NotNull T data;
    
    private final PriorityQueue<ParameterNode<?>> nextNodes = new PriorityQueue<>(
            Comparator.comparing(ParameterNode::priority)
    );
    
    protected ParameterNode(@NotNull T data) {
        this.data = data;
    }
    
    @NotNull
    public T getData() {
        return data;
    }
    
    public void addChild(ParameterNode<?> node) {
        if (nextNodes.contains(node)) return;
        nextNodes.add(node);
    }
    
    public Iterable<? extends ParameterNode<?>> getChildren() {
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
    
    public @Nullable ParameterNode<?> getChild(Predicate<ParameterNode<?>> predicate) {
        for (var child : getChildren()) {
            if (predicate.test(child)) {
                return child;
            }
        }
        return null;
    }
    
    
    public ParameterNode<?> getNextCommandChild() {
        return getChild((child) -> child instanceof CommandNode<?>);
    }
    
    public ParameterNode<?> getNextParameterChild() {
        return getChild((child) -> child instanceof CommandNode<?>);
    }
    
}
