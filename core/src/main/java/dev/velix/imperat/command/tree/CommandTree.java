package dev.velix.imperat.command.tree;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.ImperatConfig;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.*;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.Patterns;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Mqzen
 */
public final class CommandTree<S extends Source> {

    final Command<S> rootCommand;
    final CommandNode<S> root;

    CommandTree(Command<S> command) {
        this.rootCommand = command;
        this.root = new CommandNode<>(command, -1, command.getDefaultUsage());
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

    // Parsing usages part
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
        if (usage.isDefault()) {
            // For usages with no parameters, set the root as terminal with this usage
            root.setExecutableUsage(usage);
        }

        // Pass an empty list to track the path of nodes we've created
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

        if (currentNode.isGreedyParam()) {
            if (!currentNode.isLast()) {
                throw new IllegalStateException("A greedy node '%s' is not the last argument!".formatted(currentNode.format()));
            }
            currentNode.setExecutableUsage(usage);
            return;
        }

        // Find a consecutive sequence of optional flags
        List<Integer> flagSequenceIndices = new ArrayList<>();
        if (parameters.get(index).isFlag() && parameters.get(index).isOptional()) {
            flagSequenceIndices.add(index);

            // Check for additional consecutive optional flags
            for (int i = index + 1; i < parameters.size(); i++) {
                if (parameters.get(i).isFlag() && parameters.get(i).isOptional()) {
                    flagSequenceIndices.add(i);
                } else {
                    break;
                }
            }
        }

        // If we have multiple consecutive optional flags, handle them specially
        if (flagSequenceIndices.size() > 1) {
            // Handle all permutations of these flags
            handleFlagPermutations(currentNode, usage, parameters, flagSequenceIndices, path);

            // Continue with the next non-flag parameter for the case where all flags are skipped
            int nextIndex = flagSequenceIndices.get(0);
            addParametersToTree(currentNode, usage, parameters, nextIndex + 1, path);
            return;
        }

        // Regular parameter handling (non-consecutive flags)
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

    /**
     * Handles permutations of multiple consecutive optional flags.
     * Creates a branch in the command tree for each possible ordering of flags.
     */
    private void handleFlagPermutations(
            ParameterNode<S, ?> currentNode,
            CommandUsage<S> usage,
            List<CommandParameter<S>> allParameters,
            List<Integer> flagIndices,
            List<ParameterNode<S, ?>> path
    ) {
        // Extract the flag parameters
        List<CommandParameter<S>> flagParams = flagIndices.stream()
                .map(allParameters::get)
                .collect(Collectors.toList());

        // Generate all permutations of these flag parameters
        List<List<CommandParameter<S>>> permutations = generatePermutations(flagParams);

        // For each permutation, create a branch in the tree
        for (List<CommandParameter<S>> permutation : permutations) {
            ParameterNode<S, ?> nodePointer = currentNode;
            List<ParameterNode<S, ?>> updatedPath = new ArrayList<>(path);

            // Add each flag in this permutation order
            for (CommandParameter<S> flagParam : permutation) {
                ParameterNode<S, ?> flagNode = getChildNode(nodePointer, flagParam);
                updatedPath.add(flagNode);
                nodePointer = flagNode;
            }

            // Continue with the remaining parameters after the flags
            int nextIndex = flagIndices.get(flagIndices.size() - 1) + 1;
            if (nextIndex < allParameters.size()) {
                addParametersToTree(nodePointer, usage, allParameters, nextIndex, updatedPath);
            } else {
                // If there are no more parameters, mark this node as executable
                nodePointer.setExecutableUsage(usage);
            }
        }
    }

    /**
     * Generates all possible permutations of the given list of parameters
     */
    private <T> List<List<T>> generatePermutations(List<T> items) {
        if (items.isEmpty()) {
            List<List<T>> result = new ArrayList<>();
            result.add(new ArrayList<>());
            return result;
        }

        List<List<T>> result = new ArrayList<>();
        for (T item : items) {
            List<T> remaining = new ArrayList<>(items);
            remaining.remove(item);

            List<List<T>> permutationsWithoutItem = generatePermutations(remaining);

            for (List<T> perm : permutationsWithoutItem) {
                List<T> newPerm = new ArrayList<>();
                newPerm.add(item);
                newPerm.addAll(perm);
                result.add(newPerm);
            }
        }

        return result;
    }

    private ParameterNode<S, ?> getChildNode(ParameterNode<S, ?> parent, CommandParameter<S> param) {
        // Try to find an existing child node that matches this parameter
        for (ParameterNode<S, ?> child : parent.getChildren()) {
            if (child.data.name().equalsIgnoreCase(param.name())
                    && TypeUtility.matches(child.data.valueType(), param.valueType())) {
                return child;
            }
        }

        // Create a new node
        ParameterNode<S, ?> newNode;
        if (param.isCommand())
            newNode = new CommandNode<>(param.asCommand(), parent.getDepth()+1, null);
        else
            newNode = new ArgumentNode<>(param, parent.getDepth()+1, null);

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
            dispatch.setResult(CommandDispatch.Result.COMPLETE);
            dispatch.setDirectUsage(root.getExecutableUsage());
            return dispatch;
        }

        int depth = 0;

        for (ParameterNode<S, ?> child : root.getChildren()) {
            var traverse = dispatchNode(config, dispatch, input, child, depth);

            if (traverse.getResult() != CommandDispatch.Result.UNKNOWN) {
                ImperatDebugger.debug("Found a non-unknown traverse result!");
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

        if (currentNode.isGreedyParam()) {
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
            // FREE FLAG
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
        if (currentNode.isTrueFlag()) {
            depth++;
            ImperatDebugger.debug("Incrementing depth for true flag '%s', depth is now '%s'", currentNode.format(), depth);
        }

        if (isLastDepth(depth, input)) {
            // We've processed all input, check if this is a valid terminal node
            ImperatDebugger.debug("Reached last depth at depth '%s' of raw '%s'", depth, rawInput);

            if (currentNode.isExecutable()) {
                ImperatDebugger.debug("Node '%s' is executable, finished traversing!", currentNode.format());
                commandDispatch.setDirectUsage(currentNode.getExecutableUsage());
                commandDispatch.setResult(CommandDispatch.Result.COMPLETE);
                return commandDispatch;
            }

            if (currentNode.isCommand()) {
                ImperatDebugger.debug("The last node at last depth is command=%s", currentNode.format());
                addOptionalChildren(commandDispatch, currentNode);
                commandDispatch.setResult(CommandDispatch.Result.COMPLETE);
                return commandDispatch;
            }

            // Check for required parameters
            ParameterNode<S, ?> requiredParameterNode = findRequiredNodeDeeply(currentNode);
            if (requiredParameterNode == null) {
                ImperatDebugger.debug("No missing required args, it's complete now");
                commandDispatch.setResult(CommandDispatch.Result.COMPLETE);
                addOptionalChildren(commandDispatch, currentNode);
            } else {
                ImperatDebugger.debug("There are missing required args!!, the usage is UNKNOWN");
                commandDispatch.setResult(requiredParameterNode.isCommand() ? CommandDispatch.Result.COMPLETE : CommandDispatch.Result.UNKNOWN);
            }
            return commandDispatch;
        }

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
        }else {
            dispatch.setDirectUsage(childOptional.getExecutableUsage());
        }
    }

    private boolean isLastDepth(int index, ArgumentQueue input) {
        return index == input.size() - 1;
    }

    private boolean matchesInput(ImperatConfig<S> config, ParameterNode<S, ?> node, String input) {
        if (node instanceof CommandNode || config.strictCommandTree() || node.isFlag()) {
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

        // Get all valid children at once
        //HERE
        List<ParameterNode<S, ?>> validChildren = new ArrayList<>();
        List<String> skippedSimilarChildren = new ArrayList<>();
        final boolean overlapOptionalArgs = imperat.config().isOptionalParameterSuggestionOverlappingEnabled();
        
        var children = node.getChildren()
                .stream()
                .filter((child)-> root.data.isIgnoringACPerms() || imperat.config().getPermissionResolver().hasPermission(source, child.data.permission()))
                .collect(Collectors.toSet());
        
        
        for (var child : children) {
            
            if(child.isRequired()) {
                ImperatDebugger.debug("Adding required child '%s' to valid children", child.data.format());
                validChildren.add(child);
                continue;
            }
            
            // For optional nodes: add if overlapOptionalArgs is true OR no similar node exists at different depth
            ImperatDebugger.debug("Child '%s @depth=%s' is optional, checking for similar nodes", child.data.format(), child.getDepth());
            if (overlapOptionalArgs || !hasSimilarNodeWithDifferentDepth(node, child) ) {
                
                ImperatDebugger.debug("Checking if child '%s @depth=%s' is already skipped", child.data.format(), child.getDepth());
                
                if(skippedSimilarChildren.contains(child.data.format())) {
                    ImperatDebugger.debug("Skipping optional child '%s @depth=%s' - already skipped similar node", child.data.format(), child.getDepth());
                    continue;
                }
                ImperatDebugger.debug("Adding optional child '%s @depth=%s' to valid children", child.data.format(), child.getDepth());
                validChildren.add(child);
            } else {
                ImperatDebugger.debug("Skipping optional child '%s @depth=%s' - similar node exists at different depth",  child.data.format(), child.getDepth());
                ImperatDebugger.debug("Adding to skippedSimilarChildren: '%s'", child.data.format());
                skippedSimilarChildren.add(child.data.format());
            }
            
            
        }
        //DONE

        if (validChildren.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        
        // Process the first valid child and determine if we should include more
        return processValidChildren(imperat, context, validChildren);
    }

    private CompletableFuture<List<String>> processValidChildren(
            Imperat<S> imperat,
            SuggestionContext<S> context,
            List<ParameterNode<S, ?>> validChildren
    ) {
        // Pre-filter children to avoid unnecessary async calls
        List<ParameterNode<S, ?>> childrenToProcess = filterChildrenToProcess(validChildren);

        if (childrenToProcess.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        if (childrenToProcess.size() == 1) {
            // Single child optimization - no need for combining
            ParameterNode<S, ?> child = childrenToProcess.get(0);
            ImperatDebugger.debug("collecting from single child '%s'", child.data.format());
            SuggestionResolver<S> resolver = imperat.config().getParameterSuggestionResolver(child.data);
            return resolver.asyncAutoComplete(context, child.data);
        }

        // Multiple children - use parallel processing with stream
        List<CompletableFuture<List<String>>> futures = childrenToProcess.stream()
                .map(child -> {
                    ImperatDebugger.debug("collecting from child '%s'", child.data.format());
                    SuggestionResolver<S> resolver = imperat.config().getParameterSuggestionResolver(child.data);
                    return resolver.asyncAutoComplete(context, child.data);
                })
                .toList();

        // Use efficient combining with pre-sized list
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    // Pre-calculate capacity to avoid list resizing
                    int totalCapacity = futures.stream()
                            .mapToInt(f -> f.join().size())
                            .sum();

                    List<String> result = new ArrayList<>(totalCapacity);
                    futures.forEach(future -> result.addAll(future.join()));
                    return result;
                });
    }
    
    // Efficient pre-filtering to minimize async operations
    private List<ParameterNode<S, ?>> filterChildrenToProcess(List<ParameterNode<S, ?>> validChildren) {
        if (validChildren.size() <= 1) {
            return validChildren;
        }

        List<ParameterNode<S, ?>> result = new ArrayList<>();
        Set<Type> processedOptionalTypes = new HashSet<>();

        for (ParameterNode<S, ?> child : validChildren) {
            if (!child.isOptional()) {
                // Always include non-optional (like subcommands)
                result.add(child);
            } else {
                
                //child is optional
                // For optional parameters, check type uniqueness
                Type childType = child.getData().valueType();
                if (processedOptionalTypes.add(childType)) {
                    // First time seeing this type for optional params
                    result.add(child);
                }
                // Skip subsequent optional params of same type
            }
        }

        return result;
    }
    
    private boolean hasSimilarNodeWithDifferentDepth(
            ParameterNode<S, ?> currentNode,
            ParameterNode<S, ?> childNode
    ) {
        
        for(var child : currentNode.getChildren()) {
            
            ImperatDebugger.debug("Checking child '%s @depth=%s' against node '%s @depth=%s'", child.data.format(), child.getDepth(),  childNode.data.format(), childNode.getDepth());
            
            if( child.data.name().equalsIgnoreCase(childNode.data.name())
                    && child.getDepth() != childNode.getDepth()
            ) {
                ImperatDebugger.debug("Found similar node '%s @depth=%s' at different depth=%s", child.data.format(), child.getDepth(), childNode.getDepth());
                return true;
            }
            
            if (hasSimilarNodeWithDifferentDepth(child, childNode)) {
                return true;
            }
        }
        return false;
    }
    
    public ClosestUsageSearch<S> getClosestUsages(Context<S> context) {
        ArgumentQueue queue = context.arguments();
        
        var startingNode = root.getChild((child)-> {
            var raw = queue.getOr(0, null);
            if(raw == null) {
                System.out.println("RAW IS NULL");
                return true;
            }
            System.out.println("CHILD OF STARTING NODE = " + child.format());
            return child.matchesInput(raw);
        });
        System.out.println("STARTING NODE = " + (startingNode == null ? "N/A" : startingNode.format()));
        
        Set<CommandUsage<S>> closestUsages;
        
        if(startingNode == null) {
            ImperatDebugger.debug("Failed to find a starting node from the BFS step.");
            closestUsages = Set.of(rootCommand.getDefaultUsage());
        }
        else {
            ParameterNode<S, ?> closestNode = getClosestNode(startingNode, context);
            if (closestNode == null) {
                System.out.println("CLOSEST NODE = FROM STARTING NODE = NULL !");
                closestUsages = Set.of(rootCommand.getDefaultUsage());
            }else {
                closestUsages = getClosestUsagesRecursively(new LinkedHashSet<>(), closestNode, context);
            }
        }
        
        return new ClosestUsageSearch<>(closestUsages);
    }
    
    private @Nullable ParameterNode<S, ?> getClosestNode(ParameterNode<S, ?> startingNode, Context<S> context) {
        
        ArgumentQueue rawArguments = ArgumentQueue.empty();
        rawArguments.addAll(context.arguments());
        
        Queue<ParameterNode<S, ?>> queue = new LinkedList<>();
        queue.add(startingNode);
        
        String inputEntered = null;
        while (!queue.isEmpty()) {
            
            ParameterNode<S, ?> currentNode = queue.poll();
            
            if(!rawArguments.isEmpty()) {
                inputEntered = rawArguments.poll();
            }
            
            if(rawArguments.isEmpty() && inputEntered != null && currentNode.matchesInput(inputEntered) && currentNode.isExecutable()) {
                System.out.println("Found matching currentnode=" + currentNode.format());
                return currentNode;
            }
            
            System.out.println("Checking node= '" + currentNode.format() + "'");
            
            queue.addAll(
                    currentNode.getChildren().stream()
                            .filter((child)-> context.imperatConfig().getPermissionResolver().hasPermission(context.source(), child.data.permission()) )
                            .collect(Collectors.toSet())
            );
        }
        
        return null;
    }
    
    private Set<CommandUsage<S>> getClosestUsagesRecursively(
            Set<CommandUsage<S>> currentUsages,
            ParameterNode<S, ?> node,
            Context<S> context
    ) {
        if(node.isExecutable()) {
            var usage = node.getExecutableUsage();
            if(context.imperatConfig().getPermissionResolver().hasUsagePermission(context.source(),usage )) {
                currentUsages.add(usage);
            }
        }
     
        if(!node.isLast()) {
            
            for(var child : node.getChildren()) {
                String correspondingInput = context.arguments().getOr(child.getDepth(), null);
                if(correspondingInput == null ) {
                    
                    if(child.isRequired()) {
                        currentUsages.addAll(
                                getClosestUsagesRecursively(currentUsages, child, context)
                                        .stream()
                                        .filter((usage)-> context.imperatConfig().getPermissionResolver().hasUsagePermission(context.source(), usage))
                                        .collect(Collectors.toSet())
                        );
                    }
                    
                }else {
                    if( child.matchesInput(correspondingInput)  ) {
                        currentUsages.addAll(
                                getClosestUsagesRecursively(currentUsages, child, context)
                                        .stream()
                                        .filter((usage)-> context.imperatConfig().getPermissionResolver().hasUsagePermission(context.source(), usage))
                                        .collect(Collectors.toSet())
                        );
                    }
                }
                
            }
        }
      
        return currentUsages;
    }
    
}