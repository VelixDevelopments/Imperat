package dev.velix.imperat.command.tree;

import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.Nullable;

public final class CommandDispatch<S extends Source> {
    
    private ParameterNode<S, ?> lastNode;
    
    private CommandUsage<S> directUsage;

    private Result result;

    private CommandDispatch(Result result) {
        this.result = result;
    }
    
    private CommandDispatch(Result result, ParameterNode<S, ?> lastNode, CommandUsage<S> directUsage) {
        this.result = result;
        this.lastNode = lastNode;
        this.directUsage = directUsage;
    }
    
    
    static <S extends Source> CommandDispatch<S> of(final Result result) {
        return new CommandDispatch<>(result);
    }

    static <S extends Source> CommandDispatch<S> unknown() {
        return of(Result.UNKNOWN);
    }

    public void append(ParameterNode<S, ?> node) {
        if (node == null) return;
        this.lastNode = node;
    }

    public @Nullable ParameterNode<S, ?> getLastNode() {
        return lastNode;
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
    

    /**
     * Defines a setResult from dispatching the command execution.
     */
    public enum Result {

        /**
         * Defines a complete dispatch of the command,
         * {@link CommandUsage} cannot be null unless the {@link StandardCommandTree} has issues
         */
        COMPLETE,

        /**
         * Defines an unknown execution/command, it's the default setResult
         */
        UNKNOWN,

        /**
         * Defines an execution that ended up with throwing an exception that had no handler
         */
        FAILURE

    }
    
    public CommandDispatch<S> copy() {
        return new CommandDispatch<>(result, lastNode, directUsage);
    }
}
