package dev.velix.imperat.command.tree;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.ImperatConfig;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.*;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Highly optimized CommandTree implementation focused on maximum performance
 * @author Mqzen (Optimized)
 */
public final class CommandTree<S extends Source> {
    
    final Command<S> rootCommand;
    final CommandNode<S> root;
    
    // Performance optimizations - cache frequently accessed data
    private final Map<String, FlagData<S>> flagCache = new HashMap<>();
    
    // Pre-computed collections to avoid repeated allocations
    private static final List<String> EMPTY_STRING_LIST = Collections.emptyList();
    
    CommandTree(Command<S> command) {
        this.rootCommand = command;
        this.root = new CommandNode<>(command, -1, command.getDefaultUsage());
        // Pre-populate flag cache during construction
        initializeFlagCache();
    }
    
    private void initializeFlagCache() {
        // Build flag cache once during initialization
        for (var usage : rootCommand.usages()) {
            for (var flag : usage.getUsedFreeFlags()) {
                for (String alias : flag.aliases()) {
                    flagCache.put(alias, flag);
                }
            }
        }
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
    
    // Parsing usages part - optimized for reduced allocations
    public void parseCommandUsages() {
        final var usages = root.data.usages();
        for(var usage : usages) {
            parseUsage(usage);
        }
    }
    
    public void parseUsage(CommandUsage<S> usage) {
        // Register flags once
        final var flags = usage.getUsedFreeFlags();
        for (var flag : flags) {
            rootCommand.registerFlag(flag);
        }
        
        final var parameters = usage.getParameters();
        if (usage.isDefault()) {
            root.setExecutableUsage(usage);
        }
        
        // Use array instead of ArrayList for better performance with small collections
        final var path = new ArrayList<ParameterNode<S, ?>>(8); // Pre-size for typical depth
        path.add(root);
        
        addParametersToTree(root, usage, parameters, 0, path);
    }
    
    private void addParametersToTree(
            ParameterNode<S, ?> currentNode,
            CommandUsage<S> usage,
            List<CommandParameter<S>> parameters,
            int index,
            List<ParameterNode<S, ?>> path
    ) {
        // Early termination conditions - check cheapest first
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
        
        // Optimized flag sequence detection - single pass
        final int paramSize = parameters.size();
        final var flagSequenceIndices = new ArrayList<Integer>(4); // Pre-size for typical flag count
        
        if (index < paramSize) {
            final var currentParam = parameters.get(index);
            if (currentParam.isFlag() && currentParam.isOptional()) {
                flagSequenceIndices.add(index);
                
                // Single loop to find consecutive optional flags
                for (int i = index + 1; i < paramSize; i++) {
                    final var param = parameters.get(i);
                    if (param.isFlag() && param.isOptional()) {
                        flagSequenceIndices.add(i);
                    } else {
                        break;
                    }
                }
            }
        }
        
        // Handle multiple consecutive optional flags
        if (flagSequenceIndices.size() > 1) {
            handleFlagPermutations(currentNode, usage, parameters, flagSequenceIndices, path);
            addParametersToTree(currentNode, usage, parameters, flagSequenceIndices.get(0) + 1, path);
            return;
        }
        
        // Regular parameter handling - avoid redundant lookups
        final var param = parameters.get(index);
        final var childNode = getChildNode(currentNode, param);
        
        // Reuse path list to avoid allocations
        final int pathSize = path.size();
        path.add(childNode);
        
        try {
            addParametersToTree(childNode, usage, parameters, index + 1, path);
            
            if (param.isOptional()) {
                addParametersToTree(currentNode, usage, parameters, index + 1, path.subList(0, pathSize));
            }
        } finally {
            // Restore path size
            if (path.size() > pathSize) {
                path.remove(pathSize);
            }
        }
    }
    
    /**
     * Optimized flag permutation handling with reduced allocations
     */
    private void handleFlagPermutations(
            ParameterNode<S, ?> currentNode,
            CommandUsage<S> usage,
            List<CommandParameter<S>> allParameters,
            List<Integer> flagIndices,
            List<ParameterNode<S, ?>> path
    ) {
        final var flagParams = new ArrayList<CommandParameter<S>>(flagIndices.size());
        for (int idx : flagIndices) {
            flagParams.add(allParameters.get(idx));
        }
        
        final var permutations = generatePermutations(flagParams);
        final int nextIndex = flagIndices.get(flagIndices.size() - 1) + 1;
        
        for (var permutation : permutations) {
            var nodePointer = currentNode;
            final var updatedPath = new ArrayList<>(path);
            
            for (var flagParam : permutation) {
                final var flagNode = getChildNode(nodePointer, flagParam);
                updatedPath.add(flagNode);
                nodePointer = flagNode;
            }
            
            if (nextIndex < allParameters.size()) {
                addParametersToTree(nodePointer, usage, allParameters, nextIndex, updatedPath);
            } else {
                nodePointer.setExecutableUsage(usage);
            }
        }
    }
    
    /**
     * Optimized permutation generation using iterative approach
     */
    private <T> List<List<T>> generatePermutations(List<T> items) {
        if (items.isEmpty()) {
            final var result = new ArrayList<List<T>>(1);
            result.add(new ArrayList<>());
            return result;
        }
        
        final var result = new ArrayList<List<T>>();
        final int itemCount = items.size();
        
        for (int i = 0; i < itemCount; i++) {
            final T item = items.get(i);
            final var remaining = new ArrayList<T>(itemCount - 1);
            
            // Build remaining list efficiently
            for (int j = 0; j < itemCount; j++) {
                if (j != i) {
                    remaining.add(items.get(j));
                }
            }
            
            final var subPermutations = generatePermutations(remaining);
            for (var perm : subPermutations) {
                final var newPerm = new ArrayList<T>(itemCount);
                newPerm.add(item);
                newPerm.addAll(perm);
                result.add(newPerm);
            }
        }
        
        return result;
    }
    
    private ParameterNode<S, ?> getChildNode(ParameterNode<S, ?> parent, CommandParameter<S> param) {
        // Use optimized search - check commands first as they're prioritized
        final var children = parent.getChildren();
        for (var child : children) {
            if (child.data.name().equalsIgnoreCase(param.name())
                    && TypeUtility.matches(child.data.valueType(), param.valueType())) {
                return child;
            }
        }
        
        // Create new node with proper depth calculation
        final ParameterNode<S, ?> newNode = param.isCommand()
                ? new CommandNode<>(param.asCommand(), parent.getDepth() + 1, null)
                : new ArgumentNode<>(param, parent.getDepth() + 1, null);
        
        parent.addChild(newNode);
        return newNode;
    }
    
    /**
     * HEAVILY OPTIMIZED context matching - the main performance bottleneck
     */
    public @NotNull CommandDispatch<S> contextMatch(
            ArgumentQueue input,
            ImperatConfig<S> config
    ) {
        final var dispatch = CommandDispatch.<S>unknown();
        dispatch.append(root);
        
        if (input.isEmpty()) {
            dispatch.setResult(CommandDispatch.Result.COMPLETE);
            dispatch.setDirectUsage(root.getExecutableUsage());
            return dispatch;
        }
        
        final var rootChildren = root.getChildren();
        if (rootChildren.isEmpty()) {
            return dispatch;
        }
        
        // Try all children with early termination
        for (var child : rootChildren) {
            final var result = dispatchNodeOptimized(config, dispatch, input, child, 0);
            if (result.getResult() != CommandDispatch.Result.UNKNOWN) {
                return result;
            }
        }
        
        return dispatch;
    }
    
    /**
     * Heavily optimized node dispatching with minimal allocations and maximum performance
     */
    private @NotNull CommandDispatch<S> dispatchNodeOptimized(
            ImperatConfig<S> config,
            CommandDispatch<S> commandDispatch,
            ArgumentQueue input,
            @NotNull ParameterNode<S, ?> currentNode,
            int depth
    ) {
        final int inputSize = input.size();
        
        // Early bounds check
        if (depth >= inputSize) {
            if (currentNode.isExecutable()) {
                commandDispatch.append(currentNode);
                commandDispatch.setDirectUsage(currentNode.getExecutableUsage());
                commandDispatch.setResult(CommandDispatch.Result.COMPLETE);
            }
            return commandDispatch;
        }
        
        final String rawInput = input.get(depth);
        
        // Fast path for greedy parameters
        if (currentNode.isGreedyParam()) {
            commandDispatch.append(currentNode);
            commandDispatch.setResult(CommandDispatch.Result.COMPLETE);
            commandDispatch.setDirectUsage(currentNode.getExecutableUsage());
            return commandDispatch;
        }
        
        // Optimized input matching with minimal object creation
        var workingNode = currentNode;
        final boolean strictMode = config.strictCommandTree();
        
        while (!matchesInputOptimized(workingNode, rawInput, strictMode)) {
            if (workingNode.isOptional()) {
                commandDispatch.append(workingNode);
                
                var nextWorkingNode = workingNode.getNextParameterChild();
                if (nextWorkingNode == null) {
                    if(workingNode.isExecutable()) {
                        commandDispatch.setResult(CommandDispatch.Result.COMPLETE);
                        commandDispatch.setDirectUsage(workingNode.executableUsage);
                    }
                    return commandDispatch;
                }
                workingNode = nextWorkingNode;
            } else {
                return commandDispatch;
            }
        }
        
        // Handle free flags efficiently using cached data
        if (!workingNode.isFlag() && isInputFlag(rawInput)) {
            final var flagData = getFlagFromCache(rawInput);
            if (flagData == null) {
                return commandDispatch;
            }
            final int depthIncrease = flagData.isSwitch() ? 1 : 2;
            return dispatchNodeOptimized(config, commandDispatch, input, workingNode, depth + depthIncrease);
        }
        
        commandDispatch.append(workingNode);
        
        // Handle flag depth increment
        if (workingNode.isTrueFlag()) {
            depth++;
        }
        
        final boolean isLastDepth = (depth == inputSize - 1);
        
        if (isLastDepth) {
            return handleLastDepth(commandDispatch, workingNode);
        }
        
        // Continue with children - optimized iteration
        final var children = workingNode.getChildren();
        for (var child : children) {
            final var result = dispatchNodeOptimized(config, commandDispatch, input, child, depth + 1);
            if (result.getResult() == CommandDispatch.Result.COMPLETE) {
                return result;
            }
        }
        
        return commandDispatch;
    }
    
    /**
     * Optimized last depth handling
     */
    private CommandDispatch<S> handleLastDepth(CommandDispatch<S> dispatch, ParameterNode<S, ?> node) {
        if (node.isExecutable()) {
            dispatch.setDirectUsage(node.getExecutableUsage());
            dispatch.setResult(CommandDispatch.Result.COMPLETE);
            return dispatch;
        }
        
        if (node.isCommand()) {
            addOptionalChildrenOptimized(dispatch, node);
            dispatch.setResult(CommandDispatch.Result.COMPLETE);
            return dispatch;
        }
        
        final var requiredNode = findRequiredNodeDeeply(node);
        if (requiredNode == null) {
            dispatch.setResult(CommandDispatch.Result.COMPLETE);
            addOptionalChildrenOptimized(dispatch, node);
        } else {
            dispatch.setResult(requiredNode.isCommand()
                    ? CommandDispatch.Result.COMPLETE
                    : CommandDispatch.Result.UNKNOWN);
        }
        
        return dispatch;
    }
    
    /**
     * Cached flag lookup for better performance
     */
    private @Nullable FlagData<S> getFlagFromCache(String rawInput) {
        // Fast path using pre-built cache
        return flagCache.get(rawInput.startsWith("-") ? rawInput.substring(1) : rawInput);
    }
    
    /**
     * Optimized flag checking
     */
    private static boolean isInputFlag(String input) {
        return input.length() > 1 && input.charAt(0) == '-';
    }
    
    private @Nullable ParameterNode<S, ?> findRequiredNodeDeeply(ParameterNode<S, ?> currentNode) {
        final var children = currentNode.getChildren();
        for (var child : children) {
            if (child.isRequired()) {
                return child;
            }
            final var deepReq = findRequiredNodeDeeply(child);
            if (deepReq != null) {
                return deepReq;
            }
        }
        return null;
    }
    
    private void addOptionalChildrenOptimized(CommandDispatch<S> dispatch, ParameterNode<S, ?> currentNode) {
        var current = currentNode;
        while (current != null) {
            final var childOptional = getOptionalChild(current);
            if (childOptional == null) {
                break;
            }
            
            dispatch.append(childOptional);
            
            if (childOptional.isLast()) {
                dispatch.setDirectUsage(childOptional.getExecutableUsage());
                break;
            }
            current = childOptional;
        }
    }
    
    private @Nullable ParameterNode<S, ?> getOptionalChild(ParameterNode<S, ?> node) {
        final var children = node.getChildren();
        for (var child : children) {
            if (child.isOptional()) {
                return child;
            }
        }
        return null;
    }
    
    /**
     * Optimized input matching with reduced method calls
     */
    private static <S extends Source> boolean matchesInputOptimized(
            ParameterNode<S, ?> node,
            String input,
            boolean strictMode
    ) {
        if (node instanceof CommandNode || strictMode || node.isFlag()) {
            return node.matchesInput(input);
        }
        return true;
    }
    
    // Tab completion optimization - reduced allocations and improved performance
    public @NotNull CompletableFuture<List<String>> tabComplete(Imperat<S> imperat, SuggestionContext<S> context) {
        final int depthToReach = context.getArgToComplete().index();
        final var arguments = context.arguments();
        final var source = context.source();
        final var permissionResolver = imperat.config().getPermissionResolver();
        final boolean ignorePerms = root.data.isIgnoringACPerms();
        
        ParameterNode<S, ?> node = root;
        for (int i = 0; i < depthToReach; i++) {
            final String raw = arguments.getOr(i, null);
            if (raw == null) break;
            
            node = findMatchingChild(node, raw, source, permissionResolver, ignorePerms, imperat.config());
            if (node == null) break;
        }
        
        return processTabCompletionOptimized(imperat, context, node, source, permissionResolver, ignorePerms);
    }
    
    /**
     * Optimized child finding with early termination
     */
    private ParameterNode<S, ?> findMatchingChild(
            ParameterNode<S, ?> parent,
            String raw,
            S source,
            Object permissionResolver,
            boolean ignorePerms,
            ImperatConfig<S> config
    ) {
        final var children = parent.getChildren();
        for (var child : children) {
            if ((ignorePerms || hasPermission(permissionResolver, source, child.data.permission()))
                    && matchesInputOptimized(child, raw, config.strictCommandTree())) {
                return child;
            }
        }
        return null;
    }
    
    /**
     * Type-safe permission checking
     */
    @SuppressWarnings("unchecked")
    private boolean hasPermission(Object resolver, S source, String permission) {
        if (resolver instanceof dev.velix.imperat.resolvers.PermissionResolver) {
            return ((dev.velix.imperat.resolvers.PermissionResolver<S>) resolver).hasPermission(source, permission);
        }
        return true; // Fallback
    }
    
    /**
     * Heavily optimized tab completion processing
     */
    private CompletableFuture<List<String>> processTabCompletionOptimized(
            Imperat<S> imperat,
            SuggestionContext<S> context,
            ParameterNode<S, ?> node,
            S source,
            Object permissionResolver,
            boolean ignorePerms
    ) {
        if (node == null) {
            return CompletableFuture.completedFuture(EMPTY_STRING_LIST);
        }
        
        final boolean overlapOptionalArgs = imperat.config().isOptionalParameterSuggestionOverlappingEnabled();
        final var validChildren = new ArrayList<ParameterNode<S, ?>>(8);
        final var skippedNames = new HashSet<String>(8);
        
        final var children = node.getChildren();
        for (var child : children) {
            if (!ignorePerms && !hasPermission(permissionResolver, source, child.data.permission())) {
                continue;
            }
            
            if (child.isRequired()) {
                validChildren.add(child);
            } else if (overlapOptionalArgs || !hasSimilarNodeWithDifferentDepthOptimized(node, child)) {
                final String format = child.data.format();
                if (!skippedNames.contains(format)) {
                    validChildren.add(child);
                } else {
                    skippedNames.add(format);
                }
            }
        }
        
        if (validChildren.isEmpty()) {
            return CompletableFuture.completedFuture(EMPTY_STRING_LIST);
        }
        
        return processValidChildrenOptimized(imperat, context, validChildren);
    }
    
    /**
     * Optimized valid children processing with better parallelization
     */
    private CompletableFuture<List<String>> processValidChildrenOptimized(
            Imperat<S> imperat,
            SuggestionContext<S> context,
            List<ParameterNode<S, ?>> validChildren
    ) {
        final var childrenToProcess = filterChildrenToProcessOptimized(validChildren);
        
        if (childrenToProcess.isEmpty()) {
            return CompletableFuture.completedFuture(EMPTY_STRING_LIST);
        }
        
        if (childrenToProcess.size() == 1) {
            final var child = childrenToProcess.get(0);
            final var resolver = imperat.config().getParameterSuggestionResolver(child.data);
            return resolver.asyncAutoComplete(context, child.data);
        }
        
        // Parallel processing with pre-sized result collection
        final var futures = new ArrayList<CompletableFuture<List<String>>>(childrenToProcess.size());
        for (var child : childrenToProcess) {
            final var resolver = imperat.config().getParameterSuggestionResolver(child.data);
            futures.add(resolver.asyncAutoComplete(context, child.data));
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    final var result = new ArrayList<String>(64); // Pre-size for typical completion count
                    futures.forEach(future -> result.addAll(future.join()));
                    return result;
                });
    }
    
    /**
     * Optimized children filtering with reduced allocations
     */
    private List<ParameterNode<S, ?>> filterChildrenToProcessOptimized(List<ParameterNode<S, ?>> validChildren) {
        if (validChildren.size() <= 1) {
            return validChildren;
        }
        
        final var result = new ArrayList<ParameterNode<S, ?>>(validChildren.size());
        final var processedOptionalTypes = new HashSet<Type>(8);
        
        for (var child : validChildren) {
            if (!child.isOptional()) {
                result.add(child);
            } else {
                final Type childType = child.getData().valueType();
                if (processedOptionalTypes.add(childType)) {
                    result.add(child);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Optimized similar node detection with early termination
     */
    private boolean hasSimilarNodeWithDifferentDepthOptimized(
            ParameterNode<S, ?> currentNode,
            ParameterNode<S, ?> childNode
    ) {
        final String childName = childNode.data.name();
        final int childDepth = childNode.getDepth();
        
        return hasSimilarNodeRecursive(currentNode, childName, childDepth);
    }
    
    private boolean hasSimilarNodeRecursive(ParameterNode<S, ?> node, String targetName, int targetDepth) {
        final var children = node.getChildren();
        for (var child : children) {
            if (child.data.name().equalsIgnoreCase(targetName) && child.getDepth() != targetDepth) {
                return true;
            }
            if (hasSimilarNodeRecursive(child, targetName, targetDepth)) {
                return true;
            }
        }
        return false;
    }
    
    // Optimized usage search with reduced allocations
    public ClosestUsageSearch<S> getClosestUsages(Context<S> context) {
        final var queue = context.arguments();
        final String firstArg = queue.getOr(0, null);
        
        final var startingNode = (firstArg == null) ? root : findStartingNode(root, firstArg);
        
        final Set<CommandUsage<S>> closestUsages = (startingNode == null)
                ? Set.of(rootCommand.getDefaultUsage())
                : getClosestUsagesRecursively(new LinkedHashSet<>(), startingNode, context);
        
        return new ClosestUsageSearch<>(closestUsages);
    }
    
    private ParameterNode<S, ?> findStartingNode(ParameterNode<S, ?> root, String raw) {
        final var children = root.getChildren();
        for (var child : children) {
            if (child.matchesInput(raw)) {
                return child;
            }
        }
        return null;
    }
    
    /**
     * Optimized recursive usage search with permission filtering
     */
    private Set<CommandUsage<S>> getClosestUsagesRecursively(
            Set<CommandUsage<S>> currentUsages,
            ParameterNode<S, ?> node,
            Context<S> context
    ) {
        if (node.isExecutable()) {
            final var usage = node.getExecutableUsage();
            if (context.imperatConfig().getPermissionResolver().hasUsagePermission(context.source(), usage)) {
                currentUsages.add(usage);
            }
        }
        
        if (!node.isLast()) {
            final var children = node.getChildren();
            final var arguments = context.arguments();
            final var permissionResolver = context.imperatConfig().getPermissionResolver();
            final var source = context.source();
            
            for (var child : children) {
                final String correspondingInput = arguments.getOr(child.getDepth(), null);
                
                if (correspondingInput == null) {
                    if (child.isRequired()) {
                        addPermittedUsages(currentUsages, child, context, permissionResolver, source);
                    }
                } else if (child.matchesInput(correspondingInput)) {
                    addPermittedUsages(currentUsages, child, context, permissionResolver, source);
                }
            }
        }
        
        return currentUsages;
    }
    
    /**
     * Helper method to add permitted usages efficiently
     */
    private void addPermittedUsages(
            Set<CommandUsage<S>> currentUsages,
            ParameterNode<S, ?> child,
            Context<S> context,
            Object permissionResolver,
            S source
    ) {
        final var childUsages = getClosestUsagesRecursively(new LinkedHashSet<>(), child, context);
        for (var usage : childUsages) {
            if (hasUsagePermission(permissionResolver, source, usage)) {
                currentUsages.add(usage);
            }
        }
    }
    
    /**
     * Type-safe usage permission checking
     */
    @SuppressWarnings("unchecked")
    private boolean hasUsagePermission(Object resolver, S source, CommandUsage<S> usage) {
        if (resolver instanceof dev.velix.imperat.resolvers.PermissionResolver) {
            return ((dev.velix.imperat.resolvers.PermissionResolver<S>) resolver).hasUsagePermission(source, usage);
        }
        return true; // Fallback
    }
}