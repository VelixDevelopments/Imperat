package dev.velix.imperat.command.tree;

import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.ImperatDebugger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class CommandDispatch<S extends Source> implements Iterable<ParameterNode<S, ?>> {

    private final List<ParameterNode<S, ?>> nodes = new ArrayList<>();

    private CommandUsage<S> directUsage;

    private Result result;

    private CommandDispatch(Result result) {
        this.result = result;
    }

    static <S extends Source> CommandDispatch<S> of(final Result result) {
        return new CommandDispatch<>(result);
    }

    static <S extends Source> CommandDispatch<S> unknown() {
        return of(Result.UNKNOWN);
    }

    public void append(ParameterNode<S, ?> node) {
        if (node == null) return;
        //if (parameters.contains(node.data)) return;
        nodes.add(node);
    }

    public @Nullable ParameterNode<S, ?> getLastNode() {
        int lastIndex = nodes.size()-1;
        if(lastIndex < 0) {
            return null;
        }
        return nodes.get(nodes.size() - 1);
    }

    @Override
    public @NotNull Iterator<ParameterNode<S, ?>> iterator() {
        return nodes.iterator();
    }


    public void setDirectUsage(CommandUsage<S> directUsage) {
        this.directUsage = directUsage;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public @Nullable CommandUsage<S> toUsage() {
        return directUsage;
    }

    public void visualize() {
        StringBuilder builder = new StringBuilder();
        int size = nodes.size();

        int i = 0;
        for (var node : nodes) {
            builder.append(node.format());
            if (i != size - 1) {
                builder.append(" -> ");
            }
            i++;
        }
    }

    /**
     * Defines a setResult from dispatching the command execution.
     */
    public enum Result {

        /**
         * Defines a complete dispatch of the command,
         * {@link CommandUsage} cannot be null unless the {@link CommandTree} has issues
         */
        COMPLETE,

        /**
         * Defines an unknown execution/command, it's the default setResult
         */
        UNKNOWN,

        /**
         * Defines an execution that ended up with throwing an exception that had no handler
         */
        FAILURE;

    }
}
