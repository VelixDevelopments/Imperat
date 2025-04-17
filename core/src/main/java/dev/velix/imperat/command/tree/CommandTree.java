package dev.velix.imperat.command.tree;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.ImperatConfig;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        this.root = new CommandNode<>(command, command.getDefaultUsage());
        //parse(command);
    }
    public CommandNode<S> getRoot() {
        return root;
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
            // For usages with no parameters, set the root as terminal with this usage
            root.setExecutableUsage(usage);
            return;
        }

        // We'll pass an empty list to track the path of nodes we've created
        List<ParameterNode<S, ?>> path = new ArrayList<>();
        path.add(root);

        // Add parameters to the tree, handling optional parameters
        addParametersToTree(root, usage, parameters, 0, path);
    }

    private void addParametersToTree(
            ParameterNode<S, ?> currentNode,
            CommandUsage<S> usage,
            List<CommandParameter<S>> parameters,
            int index,
            List<ParameterNode<S, ?>> path
    ) {
        // If we've processed all parameters, mark the current node as terminal
        if (index >= parameters.size()) {
            currentNode.setExecutableUsage(usage);
            return;
        }

        if(currentNode.isGreedyParam()) {
            if(!currentNode.isLast()) {
                throw new IllegalStateException("A greedy node '%s' is not the last argument !".formatted(currentNode.format()));
            }
            currentNode.setExecutableUsage(usage);
            return;
        }

        CommandParameter<S> param = parameters.get(index);

        // Create/get the child node for this parameter
        ParameterNode<S, ?> childNode = getChildNode(currentNode, param);

        // Update our path
        List<ParameterNode<S, ?>> updatedPath = new ArrayList<>(path);
        updatedPath.add(childNode);

        // Continue with this parameter
        addParametersToTree(childNode, usage, parameters, index + 1, updatedPath);

        // If parameter is optional, also create a path WITHOUT this parameter
        if (param.isOptional()) {
            // Skip this optional parameter and process the next one with the current node
            addParametersToTree(currentNode, usage, parameters, index + 1, path);
        }
    }

    private ParameterNode<S, ?> getChildNode(ParameterNode<S, ?> parent, CommandParameter<S> param) {
        // Try to find an existing child node that matches this parameter
        for (ParameterNode<S, ?> child : parent.getChildren()) {
            if (child.data.name().equalsIgnoreCase(param.name())
                    && TypeUtility.matches(child.data.valueType(), param.valueType())) {
                return child;
            }
        }

        // Create a new node - both constructors now accept a usage parameter (initially null)
        ParameterNode<S, ?> newNode;
        if (param.isCommand())
            newNode = new CommandNode<>(param.asCommand(), null);
        else
            newNode = new ArgumentNode<>(param, null);

        parent.addChild(newNode);
        return newNode;
    }



    // Update the context matching to leverage terminal usages
    public @NotNull CommandDispatch<S> contextMatch(
            ArgumentQueue input,
            ImperatConfig<S> config
    ) {
        CommandDispatch<S> dispatch = CommandDispatch.unknown();
        dispatch.append(root);

        if (input.isEmpty()) {
            dispatch.setResult(CommandDispatch.Result.INCOMPLETE);
            dispatch.setDirectUsage(root.getExecutableUsage());
            return dispatch;
        }

        int depth = 0;

        for (ParameterNode<S, ?> child : root.getChildren()) {
            var traverse = dispatchNode(config, dispatch, input, child, depth);

            if (traverse.getResult() != CommandDispatch.Result.UNKNOWN) {
                ImperatDebugger.debug("Found a non-unknown traverse result !");
                return traverse;
            }
        }

        return dispatch;
    }

    private @NotNull CommandDispatch<S> dispatchNode(
            ImperatConfig<S> config,
            CommandDispatch<S> commandDispatch,
            ArgumentQueue input,
            @NotNull ParameterNode<S, ?> currentNode,
            int depth
    ) {
        if (depth >= input.size()) {
            // We've matched all input tokens
            if (currentNode.isExecutable()) {
                // This is a valid terminal node with a stored usage
                commandDispatch.append(currentNode);
                commandDispatch.setDirectUsage(currentNode.getExecutableUsage());
                commandDispatch.setResult(CommandDispatch.Result.COMPLETE);
                return commandDispatch;
            }
            return commandDispatch;
        }

        String rawInput = input.get(depth);

        if(currentNode.isGreedyParam()) {
            commandDispatch.append(currentNode);
            commandDispatch.setResult(CommandDispatch.Result.COMPLETE);
            commandDispatch.setDirectUsage(currentNode.getExecutableUsage());
            return commandDispatch;
        }

        ImperatDebugger.debug("Current depth=%s, node=%s", depth, currentNode.format());
        while (!matchesInput(config, currentNode, rawInput)) {
            if (currentNode.isOptional()) {
                ImperatDebugger.debug("Current Node '%s' doesn't match raw input '%s', while being optional", currentNode.format(), rawInput);
                commandDispatch.append(currentNode);
                currentNode = currentNode.getNextParameterChild();
            } else {
                ImperatDebugger.debug("Node '%s' doesn't match input '%s'", currentNode.format(), rawInput);
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
            return dispatchNode(config, commandDispatch, input, currentNode, depth + depthIncrease);
        }

        ImperatDebugger.debug("Appending node=%s, at depth=%s", currentNode.format(), depth);
        commandDispatch.append(currentNode);
        if(currentNode.isTrueFlag()) {
            depth++;
            ImperatDebugger.debug("Incrementing depth for true flag '%s', depth is now '%s'", currentNode.format(), depth);
        }

        if (isLastDepth(depth, input)) {
            // We've processed all input, check if this is a valid terminal node
            ImperatDebugger.debug("Reached last depth at depth '%s' of raw '%s'", depth, rawInput);

            if (currentNode.isExecutable()) {
                ImperatDebugger.debug("Node '%s' is executable, finished traversing !", currentNode.format());
                commandDispatch.setDirectUsage(currentNode.getExecutableUsage());
                commandDispatch.setResult(CommandDispatch.Result.COMPLETE);
                return commandDispatch;
            }

            if (currentNode.isCommand()) {
                ImperatDebugger.debug("The last node at last depth is command=%s", currentNode.format());
                addOptionalChildren(commandDispatch, currentNode);
                commandDispatch.setResult(CommandDispatch.Result.INCOMPLETE);
                return commandDispatch;
            }

            // Check for required parameters
            ParameterNode<S, ?> requiredParameterNode = findRequiredNodeDeeply(currentNode);
            if (requiredParameterNode == null) {
                ImperatDebugger.debug("No missing required args, it's complete now");
                commandDispatch.setResult(CommandDispatch.Result.COMPLETE);
                addOptionalChildren(commandDispatch, currentNode);
            } else {
                ImperatDebugger.debug("There are missing required args !!, the usage is UNKNOWN");
                commandDispatch.setResult(requiredParameterNode.isCommand() ? CommandDispatch.Result.COMPLETE : CommandDispatch.Result.UNKNOWN);
            }
            return commandDispatch;
        }

        /*if(currentNode.isTrueFlag()) {
            //a true flag
            depth++;
        }*/

        // Not at the last depth, continue traversing children
        for (var child : currentNode.getChildren()) {
            var result = dispatchNode(config, commandDispatch, input, child, depth + 1);
            if (result.getResult() == CommandDispatch.Result.COMPLETE) {
                return result;
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


    private boolean matchesInput(ImperatConfig<S> config, ParameterNode<S, ?> node, String input) {
        if(node instanceof CommandNode || config.strictCommandTree()) {
            return node.matchesInput(input);
        }
        return true;
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
                boolean matches = this.matchesInput(imperat.config(), c, raw);

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
}
