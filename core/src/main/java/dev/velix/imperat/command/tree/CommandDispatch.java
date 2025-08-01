package dev.velix.imperat.command.tree;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.Nullable;

public final class CommandDispatch<S extends Source> {
    
    private ParameterNode<S, ?> lastNode;
    private CommandNode<S> lastCommandNode;
    
    private CommandUsage<S> directUsage, closestUsage;

    private Result result;
    
    private CommandDispatch(Result result) {
        this.result = result;
    }
    
    private CommandDispatch(Result result, ParameterNode<S, ?> lastNode, CommandUsage<S> directUsage) {
        this.result = result;
        this.lastNode = lastNode;
        this.directUsage = directUsage;
    }
    
    public static <S extends Source> CommandDispatch<S> of(final Result result) {
        return new CommandDispatch<>(result);
    }

    public static <S extends Source> CommandDispatch<S> unknown() {
        return of(Result.UNKNOWN);
    }
    
    public static <S extends Source> CommandDispatch<S> freshlyNew(Command<S> command) {
        CommandDispatch<S> dispatch = of(Result.UNKNOWN);
        dispatch.append(command.tree().rootNode());
        dispatch.setDirectUsage(command.getDefaultUsage());
        return dispatch;
    }
    
    @SuppressWarnings("unchecked")
    public void append(ParameterNode<S, ?> node) {
        if (node == null) return;
        if(node.isCommand()) {
            this.lastCommandNode = (CommandNode<S>) node;
        }
        this.lastNode = node;
    }

    public @Nullable ParameterNode<S, ?> getLastNode() {
        return lastNode;
    }
    
    public @Nullable CommandUsage<S> getFoundUsage() {
        return directUsage;
    }

    public Result getResult() {
        return result;
    }
    
    public CommandUsage<S> getClosestUsage() {
        if(closestUsage == null) {
            //lazy computation, only when demanded for better performance :D
            closestUsage = computeClosestUsage();
        }
        
        return closestUsage;
    }
    
    public void setResult(Result result) {
        this.result = result;
    }
    
    public void setDirectUsage(CommandUsage<S> directUsage) {
        this.directUsage = directUsage;
    }
    
    private CommandUsage<S> computeClosestUsage() {
        if(directUsage != null) {
            if(lastNode != null && lastNode.isLast()) {
                return directUsage;
            }
            System.out.println("GOING FOR OTHER");
        }
        
        return (closestUsage = closestUsageLookup());
    }
    
    private CommandUsage<S> closestUsageLookup() {
        if(lastNode == null) {
            System.out.println("NODE IS NULL?!");
            return null;
        }
        CommandUsage<S> closestUsage = null;
        
        ParameterNode<S, ?> curr = lastNode;
        while (curr != null) {
            
            if(curr.isExecutable()) {
                closestUsage = curr.getExecutableUsage();
                break;
            }
            
            curr = curr.getTopChild();
        }
        
        if(closestUsage == null && lastCommandNode != null) {
            //if its still null, then let's go back to the last cmd node
            if(lastCommandNode.isExecutable()) {
                closestUsage = lastCommandNode.getExecutableUsage();
            }else {
                closestUsage = lastCommandNode.getData().getDefaultUsage();
            }
        }
        
        return closestUsage;
    }
    
    public CommandDispatch<S> copy() {
        return new CommandDispatch<>(result, lastNode, directUsage);
    }
    
    public CommandNode<S> getLastCommandNode() {
        return lastCommandNode;
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
}
