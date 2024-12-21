package dev.velix.imperat.command.tree;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        var source = context.source();
        ParameterNode<S, ?> node = root;
        for (int i = 0; i < depthToReach; i++) {
            String raw = context.arguments().getOr(i, null);
            if (raw == null) {
                break;
            }
            var child = node.getChild((c) -> {
                boolean hasPerm = (root.data.isIgnoringACPerms() || imperat.config().getPermissionResolver()
                    .hasPermission(source, c.data.permission()));
                boolean matches = c.matchesInput(raw);

                return hasPerm && matches;
            });
            if (child == null) {
                break;
            }
            node = child;
        }

        //ImperatDebugger.debug("Node-data= '%s'", node.data.format());
        CompletableFuture<Collection<String>> future = CompletableFuture.completedFuture(new ArrayList<>());
        for (var child : node.getChildren()) {
            future = future.thenCompose((results) -> addChildResults(imperat, context, child, results));
        }
        return future;
    }



    private CompletableFuture<Collection<String>> addChildResults(
        Imperat<S> imperat,
        SuggestionContext<S> context,
        ParameterNode<S, ?> node,
        Collection<String> oldResults
    ) {
        SuggestionResolver<S> resolver = imperat.config().getParameterSuggestionResolver(node.data);
        return resolver.asyncAutoComplete(context, node.data)
            .thenApply((results) -> {
                List<String> data = new ArrayList<>(results);
                data.removeIf((entry) -> !node.matchesInput(entry));
                return data;
            })
            .thenApply((res) -> {
                oldResults.addAll(res);
                return oldResults;
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
            CommandDispatch<S> nodeTraversing = CommandDispatch.unknown();

            var traverse = dispatchNode(nodeTraversing, input, child, depth);

            if (traverse.result() != CommandDispatch.Result.UNKNOWN) {
                return traverse;
            }

        }

        return CommandDispatch.unknown();
    }

    private @NotNull CommandDispatch<S> dispatchNode(
        CommandDispatch<S> commandDispatch,
        ArgumentQueue input,
        ParameterNode<S, ?> currentNode,
        int depth
    ) {
        if (depth >= input.size()) {
            return commandDispatch;
        }

        ImperatDebugger.debug("Current depth=%s, node=%s", depth, currentNode.format());
        if (!currentNode.matchesInput(input.get(depth))) {
            ImperatDebugger.debug("Node '%s' doesn't match input '%s'", currentNode.format(), input.get(depth));
            return commandDispatch;
        }

        ImperatDebugger.debug("Appending node=%s, at depth=%s", currentNode.format(), depth);
        commandDispatch.append(currentNode);

        if (!currentNode.isLast()) {
            if (isLastDepth(depth, input)) {
                ImperatDebugger.debug("Reached the end of the input at depth=%s", depth);
                //check for incomplete executions
                if (currentNode.isCommand()) {
                    ImperatDebugger.debug("The last node at last depth is command=%s", currentNode.format());
                    addOptionalChildren(commandDispatch, currentNode);
                    commandDispatch.result(CommandDispatch.Result.INCOMPLETE);
                    return commandDispatch;
                }

                //means that there's missing arguments that need to be input
                //check if the missing arguments are optional to ignore and complete this
                //by finding the next required argument

                ImperatDebugger.debug("Finding missing required argument");
                ParameterNode<S, ?> requiredParameterNode = findRequiredNodeDeeply(currentNode);
                if (requiredParameterNode == null) {
                    ImperatDebugger.debug("No missing required args, it's complete now");
                    //we ignore it and assume the result is complete
                    commandDispatch.result(CommandDispatch.Result.COMPLETE);
                    //but don't forget to add optionals if there are
                    addOptionalChildren(commandDispatch, currentNode);
                } else {
                    ImperatDebugger.debug("There are missing required args !!, the usage is UNKNOWN");
                    commandDispatch.result(requiredParameterNode.isCommand() ? CommandDispatch.Result.COMPLETE : CommandDispatch.Result.UNKNOWN);
                }
            } else {
                ImperatDebugger.debug("we still in the middle of the input at depth=%s", depth);
                for (var child : currentNode.getChildren()) {
                    var result = dispatchNode(commandDispatch, input, child, depth + 1);
                    if (result.result() == CommandDispatch.Result.COMPLETE) {
                        return result;
                    }
                }
            }
        } else {
            ImperatDebugger.debug("We reached the end of the node, at node=%s", currentNode.format());
            //node is the last
            if (isLastDepth(depth, input)) {
                //Last depth and last node => perfecto
                commandDispatch.result(CommandDispatch.Result.COMPLETE);
            } else {
                CommandDispatch.Result result;
                if (currentNode.isTrueFlag() && isLastDepth(depth + 1, input)) {
                    result = CommandDispatch.Result.COMPLETE;
                } else if (!currentNode.isGreedyParam()) {
                    result = CommandDispatch.Result.UNKNOWN;
                } else {
                    result = CommandDispatch.Result.COMPLETE;
                }
                commandDispatch.result(result);
            }

        }
        return commandDispatch;
    }

    private @Nullable ParameterNode<S, ?> findRequiredNodeDeeply(ParameterNode<S, ?> currentNode) {
        for (var child : currentNode.getChildren()) {
            if (child.isRequired()) {
                return child;
            } else {
                var deepReq = findRequiredNodeDeeply(child);
                if (deepReq != null) {
                    return deepReq;
                }
            }
        }
        return null;
    }

    private void addOptionalChildren(CommandDispatch<S> dispatch, ParameterNode<S, ?> currentNode) {
        ParameterNode<S, ?> childOptional = currentNode.getChild(ParameterNode::isOptional);
        if (childOptional == null) return;

        dispatch.append(childOptional);

        if (!childOptional.isLast()) {
            addOptionalChildren(dispatch, childOptional);
        }
    }

    private boolean isLastDepth(int index, ArgumentQueue input) {
        return index == input.size() - 1;
    }

    public CommandNode<S> getRoot() {
        return root;
    }

    /*private @NotNull CommandDispatch<S> contextMatchNode(
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
        if (currentNode.isLast()) {

            //not the deepest search depth (still more raw args than number of nodes)
            commandDispatch.result((depth != input.size() - 1 && !currentNode.isGreedyParam())
                ? CommandDispatch.Result.INCOMPLETE : CommandDispatch.Result.COMPLETE);
        } else {
            //not the last node → continue traversing

            //checking if depth is the last
            if(depth < input.size()-1) {
                return this.searchForMatch(currentNode, commandDispatch, input, depth);
            }
            //depth is last → check for missing required arguments
            //commandDispatch.append(currentNode);
            if (currentNode.isCommand()) {
                commandDispatch.result(CommandDispatch.Result.INCOMPLETE);
                return commandDispatch;
            }

            if (currentNode.isOptional()) {
                //so if the node is optional,
                // we go deeper into the tree, while backtracking the depth of the argument input.
                return searchForMatch(currentNode, commandDispatch, input, depth - 1);
            }
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
                    if (!child.isLast()) {
                        return searchForMatch(child, commandDispatch, input, depth - 1);
                    }
                }
            }

            //adding rest of required args
            var requiredChild = currentNode.getChild(ParameterNode::isRequired);

            if (requiredChild != null) {
                //commandDispatch.append(requiredChild);
                if (!requiredChild.isLast()) {
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

        }
        return commandDispatch;

    }
*/
   /* private CommandDispatch<S> searchForMatch(
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
    }*/


}
