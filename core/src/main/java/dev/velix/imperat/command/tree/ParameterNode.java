package dev.velix.imperat.command.tree;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class ParameterNode<S extends Source, T extends CommandParameter<S>> {

    protected final @NotNull T data;

    private final LinkedList<ParameterNode<S, ?>> nextNodes = new LinkedList<>();

    protected ParameterNode(@NotNull T data) {
        this.data = data;
    }

    @NotNull
    public T getData() {
        return data;
    }

    public void addChild(ParameterNode<S, ?> node) {
        if (nextNodes.contains(node)) return;
        if(node.isCommand()) {
            nextNodes.addFirst(node);
        }else {
            nextNodes.addLast(node);
        }
    }

    public Collection<ParameterNode<S,?>> getChildren() {
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

    public boolean isTrueFlag() {
        return this.data.isFlag() && !this.data.asFlagParameter().isSwitch();
    }

    public boolean isFlag() {
        return this.data.isFlag();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ParameterNode<?, ?> that)) return false;
        return Objects.equals(data.name(), that.data.name()) && Objects.equals(nextNodes, that.nextNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data.name(), nextNodes);
    }
}
