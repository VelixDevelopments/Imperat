package dev.velix.imperat.command.tree;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Mqzen
 */
public final class CommandTree<S extends Source> {

    final CommandNode<S> root;

    CommandTree(Command<S> command) {
        this.root = new CommandNode<>(command);
        //parse(command);
    }

    public static <S extends Source> CommandTree<S> create(Command<S> command) {
        return new CommandTree<>(command);
    }

    public static <S extends Source> CommandTree<S> parsed(Command<S> command) {
        CommandTree<S> tree = create(command);
        tree.parseCommandUsages();
        return tree;
    }

    //parsing usages part
    public void parseCommandUsages() {
        for (CommandUsage<S> usage : root.data.usages()) {
            parseUsage(usage);
        }
    }

    public void parseUsage(CommandUsage<S> usage) {
        List<CommandParameter<S>> parameters = usage.getParameters();
        if (parameters == null || parameters.isEmpty()) {
            return;
        }
        addParametersToTree(root, parameters, 0);
    }

    private void addParametersToTree(
        ParameterNode<S, ?> currentNode,
        List<CommandParameter<S>> parameters,
        int index
    ) {
        if (index >= parameters.size()) {
            return;
        }

        CommandParameter<S> param = parameters.get(index);
        ParameterNode<S, ?> childNode = getChildNode(currentNode, param);
        // Recursively add the remaining parameters to the child node
        addParametersToTree(childNode, parameters, index + 1);
    }

    private ParameterNode<S, ?> getChildNode(ParameterNode<S, ?> parent, CommandParameter<S> param) {
        for (ParameterNode<S, ?> child : parent.getChildren()) {
            if (child.data.name().equalsIgnoreCase(param.name())
                && TypeUtility.matches(child.data.valueType(), param.valueType())) {
                return child;
            }
        }
        ParameterNode<S, ?> newNode;
        if (param.isCommand())
            newNode = new CommandNode<>(param.asCommand());
        else
            newNode = new ArgumentNode<>(param);

        parent.addChild(newNode);
        return newNode;
    }

    public @NotNull CompletableFuture<Collection<String>> tabComplete(Imperat<S> imperat, SuggestionContext<S> context) {
        final int depthToReach = context.getArgToComplete().index();

        CompletableFuture<Collection<String>> future = CompletableFuture.completedFuture(new ArrayList<>());
        for (var child : root.getChildren()) {
            future = future.thenCompose((results) -> collectNodeCompletions(imperat, context, child, 0, depthToReach, results));
        }
        return future;
    }

    private CompletableFuture<Collection<String>> collectNodeCompletions(
        Imperat<S> imperat,
        SuggestionContext<S> context,
        ParameterNode<S, ?> child,
        int depth,
        final int maxDepth,
        Collection<String> results
    ) {
        if (depth > maxDepth) {
            return CompletableFuture.completedFuture(results);
        }

        String raw = context.arguments().getOr(depth, "");
        assert raw != null;

        if (raw.isEmpty() || raw.isBlank()) {
            return CompletableFuture.completedFuture(results);
        }

        boolean hasNoPermission = (!root.data.isIgnoringACPerms() && !imperat.getPermissionResolver()
            .hasPermission(context.source(), child.data.permission()));

        if (hasNoPermission) {
            //ImperatDebugger.debug("Ending tab completion quickly");
            return CompletableFuture.completedFuture(results);
        }

        if (depth == maxDepth) {
            //we reached the arg we want to complete, let's complete it using our current node
            //COMPLETE DIRECTLY
            //ImperatDebugger.debug("Adding childs results");
            return addChildResults(imperat, context, child);
        } else {
            if (child.data.isFlag() && !child.data.asFlagParameter().isSwitch()) {
                //auto completing value for flag, using SAME child/flag parameter while incrementing depth by 1
                return collectNodeCompletions(imperat, context, child, depth + 1, maxDepth, results);
            }
            CompletableFuture<Collection<String>> future = CompletableFuture.completedFuture(results);
            //Keep looking
            for (var innerChild : child.getChildren()) {
                future = future.thenCompose((res) -> collectNodeCompletions(imperat, context, innerChild, depth + 1, maxDepth, res));
            }

            return future;
        }
    }

    private CompletableFuture<Collection<String>> addChildResults(
        Imperat<S> imperat,
        SuggestionContext<S> context,
        ParameterNode<S, ?> node
    ) {
        SuggestionResolver<S> resolver = imperat.getParameterSuggestionResolver(node.data);
        return resolver.asyncAutoComplete(context, node.data)
            .thenApply((results) -> {
                results.removeIf((entry) -> !node.matchesInput(entry));
                return results;
            });
    }


    //context matching part
    public @NotNull CommandDispatch<S> contextMatch(
        ArgumentQueue input
    ) {
        if (input.isEmpty()) {
            return CommandDispatch.incomplete();
        }

        int depth = 0;

        for (ParameterNode<S, ?> child : root.getChildren()) {
            CommandDispatch<S> nodeTraversing = CommandDispatch.empty();
            var traverse = contextMatchNode(nodeTraversing, input, child, depth);

            if (traverse.result() != CommandDispatch.Result.UNKNOWN) {
                return traverse;
            }

        }

        return CommandDispatch.empty();
    }

    private @NotNull CommandDispatch<S> contextMatchNode(
        CommandDispatch<S> commandDispatch,
        ArgumentQueue input,
        ParameterNode<S, ?> currentNode,
        int depth
    ) {
        //ImperatDebugger.debug("Traversing node=%s, at depth=%s", node.format(), depth);
        if (depth >= input.size()) {
            return commandDispatch;
        }

        String raw = input.get(depth);
        boolean matchesInput = currentNode.matchesInput(raw);
        if (!matchesInput) {
            return commandDispatch;
        }
        //GO TO NODE'S children
        commandDispatch.append(currentNode);
        if (currentNode.isLeaf()) {

            //not the deepest search depth (still more raw args than number of nodes)
            commandDispatch.result((depth != input.size() - 1 && !currentNode.isGreedyParam())
                ? CommandDispatch.Result.INCOMPLETE : CommandDispatch.Result.COMPLETE);
            return commandDispatch;
        } else {
            //not the last node → continue traversing

            //checking if depth is the last
            if (depth == input.size() - 1) {
                //depth is last → check for missing required arguments
                //commandDispatch.append(currentNode);

                if (currentNode instanceof CommandNode<?>) {
                    commandDispatch.result(CommandDispatch.Result.INCOMPLETE);
                }

                if (currentNode.isOptional()) {
                    //so if the node is optional,
                    // we go deeper into the tree, while backtracking the depth of the argument input.
                    return searchForMatch(currentNode, commandDispatch, input, depth - 1);
                } else {
                    //ImperatDebugger.debug("Last Depth=%s, Current node= %s", depth, node.format());
                    //node is not the last, and we reached the end of the raw input length
                    //We check if there's any missing optional
                    boolean allOptional = true;
                    for (ParameterNode<S, ?> child : currentNode.getChildren()) {
                        if (!child.isOptional()) {
                            allOptional = false;
                            break;
                        }
                    }

                    //improved logic
                    if (allOptional) {
                        //if all optional, then append the all next
                        var child = currentNode.getChild(ParameterNode::isOptional);
                        if (child != null) {
                            commandDispatch.append(child);
                            //collect optionals while depth is constant since we reached the end of raw input early
                            if (!child.isLeaf()) {
                                return searchForMatch(child, commandDispatch, input, depth - 1);
                            }
                        }
                    }

                    //adding rest of required args
                    var requiredChild = currentNode.getChild(ParameterNode::isRequired);

                    if (requiredChild != null) {
                        commandDispatch.append(requiredChild);
                        if (!requiredChild.isLeaf()) {
                            return searchForMatch(requiredChild, commandDispatch, input, depth - 1);
                        }
                    }
                    //ImperatDebugger.debug("All optional after last depth ? = %s", (allOptional) );
                    var usage = commandDispatch.toUsage(root.data);
                    commandDispatch.result(
                        allOptional || (usage != null && !(currentNode instanceof CommandNode<?>))
                            ? CommandDispatch.Result.COMPLETE
                            : CommandDispatch.Result.INCOMPLETE
                    );
                    return commandDispatch;
                }

            } else {
                return this.searchForMatch(currentNode, commandDispatch, input, depth);
            }

        }

    }

    private CommandDispatch<S> searchForMatch(
        ParameterNode<S, ?> node,
        CommandDispatch<S> commandDispatch,
        ArgumentQueue input,
        int depth
    ) {
        for (ParameterNode<S, ?> child : node.getChildren()) {
            var traversedChild = contextMatchNode(commandDispatch, input, child, depth + 1);
            if (traversedChild.result() == CommandDispatch.Result.COMPLETE)
                return traversedChild;
        }
        return commandDispatch;
    }

    public CommandNode<S> getRoot() {
        return root;
    }
}
