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
import dev.velix.imperat.util.Patterns;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Mqzen
 */
public final class CommandTree<S extends Source> {

    final Command<S> rootCommand;
    final CommandNode<S> root;

    CommandTree(Command<S> command) {
        this.rootCommand = command;
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

        for (var flag : usage.getUsedFreeFlags()) {
            rootCommand.registerFlag(flag);
        }

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

    public @NotNull CompletableFuture<List<String>> tabComplete(Imperat<S> imperat, SuggestionContext<S> context) {
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

        ImperatDebugger.debug("Node-data= '%s'", node.data.format());
        CompletableFuture<List<String>> future = CompletableFuture.completedFuture(new ArrayList<>());
        for (var child : node.getChildren()) {
            ImperatDebugger.debug("collecting from child '%s'", child.data.format());
            future = future.thenCompose((results) -> addChildResults(imperat, context, child, results));
        }
        return future;
    }



    private CompletableFuture<List<String>> addChildResults(
        Imperat<S> imperat,
        SuggestionContext<S> context,
        ParameterNode<S, ?> node,
        List<String> oldResults
    ) {

        SuggestionResolver<S> resolver = imperat.config().getParameterSuggestionResolver(node.data);
        var currentNodeFutureResults =  resolver.asyncAutoComplete(context, node.data)
                .thenApply((res) -> {
                    oldResults.addAll(res);
                    return oldResults;
                });

        var optionalChild = node.getChild(ParameterNode::isOptional);
        if(optionalChild == null)
            return currentNodeFutureResults;

        return currentNodeFutureResults.thenCompose((results)-> addChildResults(imperat, context, optionalChild, currentNodeFutureResults.join()));
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
        @NotNull ParameterNode<S, ?> currentNode,
        int depth
    ) {
        if (depth >= input.size()) {
            return commandDispatch;
        }

        String rawInput = input.get(depth);

        ImperatDebugger.debug("Current depth=%s, node=%s", depth, currentNode.format());
        while (!currentNode.matchesInput(rawInput)) {

            if (currentNode.isOptional()) {
                commandDispatch.append(currentNode);
                currentNode = currentNode.getNextParameterChild();
            } else {
                ImperatDebugger.debug("Node '%s' doesn't match input '%s'", currentNode.format(), input.get(depth));
                return commandDispatch;
            }
        }

        if (!currentNode.isFlag() && Patterns.isInputFlag(rawInput)) {
            //FREE FLAG
            var flagData = rootCommand.getFlagFromRaw(rawInput);
            if (flagData.isEmpty()) {
                return commandDispatch;
            }
            var flag = flagData.get();
            int depthIncrease = flag.isSwitch() ? 1 : 2;
            return dispatchNode(commandDispatch, input, currentNode, depth + depthIncrease);
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
                String nextRaw = input.getOr(depth + 1, null);
                assert nextRaw != null;

                ImperatDebugger.debug("nextRaw=%s, at depth=%s, max-depth=%s", nextRaw, depth + 1, input.size() - 1);
                ImperatDebugger.debug("isInputFlag-nextRaw='%s',  isFreeFlagRegisteredToCommand='%s'", Patterns.isInputFlag(nextRaw), rootCommand.getFlagFromRaw(nextRaw).isPresent());

                CommandDispatch.Result result;
                if (currentNode.isTrueFlag() && isLastDepth(depth + 1, input)) {
                    result = CommandDispatch.Result.COMPLETE;
                } else if (Patterns.isInputFlag(nextRaw) && rootCommand.getFlagFromRaw(nextRaw).isPresent()) {
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

}
