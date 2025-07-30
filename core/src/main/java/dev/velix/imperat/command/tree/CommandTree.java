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
 * Ultra-optimized CommandTree implementation focused on maximum performance
 * Removed excessive profiling and optimized hot paths
 * @author Mqzen (Ultra-Optimized)
 */
public final class CommandTree<S extends Source> {
    final Command<S> rootCommand;
    final CommandNode<S> root;
    
    // Pre-computed immutable collections to eliminate allocations
    private static final List<String> EMPTY_STRING_LIST = Collections.emptyList();
    
    // Optimized flag cache with better hashing
    private final Map<String, FlagData<S>> flagCache;
    
    // Pre-sized collections for common operations
    private final ThreadLocal<ArrayList<ParameterNode<S, ?>>> pathBuffer =
            ThreadLocal.withInitial(() -> new ArrayList<>(16));
    private final ThreadLocal<ArrayList<CommandParameter<S>>> paramBuffer =
            ThreadLocal.withInitial(() -> new ArrayList<>(8));
    
    CommandTree(Command<S> command) {
        this.rootCommand = command;
        this.root = new CommandNode<>(command, -1, command.getDefaultUsage());
        this.flagCache = initializeFlagCache();
    }
    
    private Map<String, FlagData<S>> initializeFlagCache() {
        // Use HashMap instead of concurrent map for better performance in single-threaded access
        final var cache = new HashMap<String, FlagData<S>>();
        for (var usage : rootCommand.usages()) {
            for (var flag : usage.getUsedFreeFlags()) {
                for (String alias : flag.aliases()) {
                    cache.put(alias, flag);
                }
            }
        }
        return Collections.unmodifiableMap(cache); // Make immutable
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
    
    // Optimized parsing with reduced allocations
    public void parseCommandUsages() {
        final var usages = root.data.usages();
        for (var usage : usages) {
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
        
        // Use thread-local buffer to eliminate allocations
        final var path = pathBuffer.get();
        path.clear();
        path.add(root);
        
        try {
            addParametersToTree(root, usage, parameters, 0, path);
        } finally {
            path.clear(); // Clean up for next use
        }
    }
    
    private void addParametersToTree(
            ParameterNode<S, ?> currentNode,
            CommandUsage<S> usage,
            List<CommandParameter<S>> parameters,
            int index,
            List<ParameterNode<S, ?>> path
    ) {
        // Early termination - check cheapest conditions first
        final int paramSize = parameters.size();
        if (index >= paramSize) {
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
        
        // Optimized flag sequence detection
        int flagSequenceEnd = findFlagSequenceEnd(parameters, index);
        
        if (flagSequenceEnd > index) {
            // Handle multiple consecutive optional flags
            handleFlagSequenceOptimized(currentNode, usage, parameters, index, flagSequenceEnd, path);
            addParametersToTree(currentNode, usage, parameters, flagSequenceEnd, path);
            return;
        }
        
        // Regular parameter handling
        final var param = parameters.get(index);
        final var childNode = getOrCreateChildNode(currentNode, param);
        
        // Efficient path management
        final int pathSize = path.size();
        path.add(childNode);
        
        try {
            addParametersToTree(childNode, usage, parameters, index + 1, path);
            
            if (param.isOptional()) {
                // Create sublist view instead of new list
                addParametersToTree(currentNode, usage, parameters, index + 1,
                        path.subList(0, pathSize));
            }
        } finally {
            // Restore path size efficiently
            if (path.size() > pathSize) {
                path.remove(pathSize);
            }
        }
    }
    
    /**
     * Optimized flag sequence detection in single pass
     */
    private int findFlagSequenceEnd(List<CommandParameter<S>> parameters, int startIndex) {
        if (startIndex >= parameters.size()) return startIndex;
        
        final var startParam = parameters.get(startIndex);
        if (!startParam.isFlag() || !startParam.isOptional()) {
            return startIndex;
        }
        
        int end = startIndex + 1;
        for (int i = startIndex + 1; i < parameters.size(); i++) {
            final var param = parameters.get(i);
            if (param.isFlag() && param.isOptional()) {
                end = i + 1;
            } else {
                break;
            }
        }
        
        return end;
    }
    
    /**
     * Optimized flag permutation handling
     */
    private void handleFlagSequenceOptimized(
            ParameterNode<S, ?> currentNode,
            CommandUsage<S> usage,
            List<CommandParameter<S>> allParameters,
            int flagStart,
            int flagEnd,
            List<ParameterNode<S, ?>> path
    ) {
        final var flagParams = paramBuffer.get();
        flagParams.clear();
        
        try {
            // Collect flag parameters
            for (int i = flagStart; i < flagEnd; i++) {
                flagParams.add(allParameters.get(i));
            }
            
            // Generate and process permutations efficiently
            processPermutationsIteratively(currentNode, usage, allParameters, flagParams, flagEnd, path);
        } finally {
            flagParams.clear();
        }
    }
    
    /**
     * Iterative permutation processing to avoid recursion overhead
     */
    private void processPermutationsIteratively(
            ParameterNode<S, ?> currentNode,
            CommandUsage<S> usage,
            List<CommandParameter<S>> allParameters,
            List<CommandParameter<S>> flagParams,
            int nextIndex,
            List<ParameterNode<S, ?>> basePath
    ) {
        // For small flag sets, use simple permutation generation
        if (flagParams.size() <= 3) {
            generateSmallPermutations(currentNode, usage, allParameters, flagParams, nextIndex, basePath);
        } else {
            // For larger sets, use optimized iterative approach
            generateLargePermutations(currentNode, usage, allParameters, flagParams, nextIndex, basePath);
        }
    }
    
    private void generateSmallPermutations(
            ParameterNode<S, ?> currentNode,
            CommandUsage<S> usage,
            List<CommandParameter<S>> allParameters,
            List<CommandParameter<S>> flagParams,
            int nextIndex,
            List<ParameterNode<S, ?>> basePath
    ) {
        // Direct handling for 1-3 flags to avoid overhead
        final int size = flagParams.size();
        if (size == 1) {
            processSinglePermutation(currentNode, usage, allParameters, flagParams, nextIndex, basePath);
        } else if (size == 2) {
            // Handle 2! = 2 permutations directly
            processTwoPermutations(currentNode, usage, allParameters, flagParams, nextIndex, basePath);
        } else {
            // Handle 3! = 6 permutations directly
            processThreePermutations(currentNode, usage, allParameters, flagParams, nextIndex, basePath);
        }
    }
    
    private void processSinglePermutation(
            ParameterNode<S, ?> currentNode,
            CommandUsage<S> usage,
            List<CommandParameter<S>> allParameters,
            List<CommandParameter<S>> flagParams,
            int nextIndex,
            List<ParameterNode<S, ?>> basePath
    ) {
        final var flagNode = getOrCreateChildNode(currentNode, flagParams.get(0));
        final var updatedPath = new ArrayList<>(basePath);
        updatedPath.add(flagNode);
        
        if (nextIndex < allParameters.size()) {
            addParametersToTree(flagNode, usage, allParameters, nextIndex, updatedPath);
        } else {
            flagNode.setExecutableUsage(usage);
        }
    }
    
    private void processTwoPermutations(
            ParameterNode<S, ?> currentNode,
            CommandUsage<S> usage,
            List<CommandParameter<S>> allParameters,
            List<CommandParameter<S>> flagParams,
            int nextIndex,
            List<ParameterNode<S, ?>> basePath
    ) {
        final var flag1 = flagParams.get(0);
        final var flag2 = flagParams.get(1);
        
        // Permutation 1: [flag1, flag2]
        processPermutationPath(currentNode, usage, allParameters, List.of(flag1, flag2), nextIndex, basePath);
        
        // Permutation 2: [flag2, flag1]
        processPermutationPath(currentNode, usage, allParameters, List.of(flag2, flag1), nextIndex, basePath);
    }
    
    private void processThreePermutations(
            ParameterNode<S, ?> currentNode,
            CommandUsage<S> usage,
            List<CommandParameter<S>> allParameters,
            List<CommandParameter<S>> flagParams,
            int nextIndex,
            List<ParameterNode<S, ?>> basePath
    ) {
        final var flag1 = flagParams.get(0);
        final var flag2 = flagParams.get(1);
        final var flag3 = flagParams.get(2);
        
        // All 6 permutations of 3 flags
        processPermutationPath(currentNode, usage, allParameters, List.of(flag1, flag2, flag3), nextIndex, basePath);
        processPermutationPath(currentNode, usage, allParameters, List.of(flag1, flag3, flag2), nextIndex, basePath);
        processPermutationPath(currentNode, usage, allParameters, List.of(flag2, flag1, flag3), nextIndex, basePath);
        processPermutationPath(currentNode, usage, allParameters, List.of(flag2, flag3, flag1), nextIndex, basePath);
        processPermutationPath(currentNode, usage, allParameters, List.of(flag3, flag1, flag2), nextIndex, basePath);
        processPermutationPath(currentNode, usage, allParameters, List.of(flag3, flag2, flag1), nextIndex, basePath);
    }
    
    private void processPermutationPath(
            ParameterNode<S, ?> currentNode,
            CommandUsage<S> usage,
            List<CommandParameter<S>> allParameters,
            List<CommandParameter<S>> permutation,
            int nextIndex,
            List<ParameterNode<S, ?>> basePath
    ) {
        var nodePointer = currentNode;
        final var updatedPath = new ArrayList<>(basePath);
        
        for (var flagParam : permutation) {
            final var flagNode = getOrCreateChildNode(nodePointer, flagParam);
            updatedPath.add(flagNode);
            nodePointer = flagNode;
        }
        
        if (nextIndex < allParameters.size()) {
            addParametersToTree(nodePointer, usage, allParameters, nextIndex, updatedPath);
        } else {
            nodePointer.setExecutableUsage(usage);
        }
    }
    
    private void generateLargePermutations(
            ParameterNode<S, ?> currentNode,
            CommandUsage<S> usage,
            List<CommandParameter<S>> allParameters,
            List<CommandParameter<S>> flagParams,
            int nextIndex,
            List<ParameterNode<S, ?>> basePath
    ) {
        // For larger sets, use Heap's algorithm iteratively
        // This is more complex but more efficient for larger permutation sets
        final int n = flagParams.size();
        final int[] indices = new int[n];
        
        // First permutation (identity)
        processPermutationPath(currentNode, usage, allParameters, new ArrayList<>(flagParams), nextIndex, basePath);
        
        int i = 0;
        while (i < n) {
            if (indices[i] < i) {
                // Swap elements
                if (i % 2 == 0) {
                    Collections.swap(flagParams, 0, i);
                } else {
                    Collections.swap(flagParams, indices[i], i);
                }
                
                // Process this permutation
                processPermutationPath(currentNode, usage, allParameters, new ArrayList<>(flagParams), nextIndex, basePath);
                
                indices[i]++;
                i = 0;
            } else {
                indices[i] = 0;
                i++;
            }
        }
    }
    
    private ParameterNode<S, ?> getOrCreateChildNode(ParameterNode<S, ?> parent, CommandParameter<S> param) {
        // Optimized child lookup with early termination
        final var children = parent.getChildren();
        final String paramName = param.name();
        final Type paramType = param.valueType();
        
        for (var child : children) {
            if (child.data.name().equalsIgnoreCase(paramName) &&
                    TypeUtility.matches(child.data.valueType(), paramType)) {
                return child;
            }
        }
        
        // Create new node
        final ParameterNode<S, ?> newNode = param.isCommand()
                ? new CommandNode<>(param.asCommand(), parent.getDepth() + 1, null)
                : new ArgumentNode<>(param, parent.getDepth() + 1, null);
        
        parent.addChild(newNode);
        return newNode;
    }
    
    /**
     * Ultra-optimized context matching - removed excessive profiling
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
        
        // Process children efficiently
        for (var child : rootChildren) {
            final var result = dispatchNode(config, dispatch, input, child, 0);
            if (result.getResult() != CommandDispatch.Result.UNKNOWN) {
                return result;
            }
        }
        
        return dispatch;
    }
    
    /**
     * Streamlined node dispatching without profiling overhead
     */
    private @NotNull CommandDispatch<S> dispatchNode(
            ImperatConfig<S> config,
            CommandDispatch<S> commandDispatch,
            ArgumentQueue input,
            @NotNull ParameterNode<S, ?> currentNode,
            int depth
    ) {
        // Bounds check
        final int inputSize = input.size();
        if (depth >= inputSize) {
            if (currentNode.isExecutable()) {
                commandDispatch.append(currentNode);
                commandDispatch.setDirectUsage(currentNode.getExecutableUsage());
                commandDispatch.setResult(CommandDispatch.Result.COMPLETE);
            }
            return commandDispatch;
        }
        
        final String rawInput = input.get(depth);
        
        // Greedy parameter check
        if (currentNode.isGreedyParam()) {
            commandDispatch.append(currentNode);
            commandDispatch.setResult(CommandDispatch.Result.COMPLETE);
            commandDispatch.setDirectUsage(currentNode.getExecutableUsage());
            return commandDispatch;
        }
        
        // Input matching loop with reduced overhead
        var workingNode = currentNode;
        final boolean strictMode = config.strictCommandTree();
        
        while (!matchesInput(workingNode, rawInput, strictMode)) {
            if (workingNode.isOptional()) {
                commandDispatch.append(workingNode);
                
                var nextWorkingNode = workingNode.getNextParameterChild();
                if (nextWorkingNode == null) {
                    if (workingNode.isExecutable()) {
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
        
        // Flag handling with cached lookup
        if (!workingNode.isFlag() && isFlag(rawInput)) {
            final var flagData = flagCache.get(rawInput.substring(1));
            if (flagData == null) {
                return commandDispatch;
            }
            final int depthIncrease = flagData.isSwitch() ? 1 : 2;
            return dispatchNode(config, commandDispatch, input, workingNode, depth + depthIncrease);
        }
        
        commandDispatch.append(workingNode);
        
        if (workingNode.isTrueFlag()) {
            depth++;
        }
        
        final boolean isLastDepth = (depth == inputSize - 1);
        
        if (isLastDepth) {
            return handleLastDepth(commandDispatch, workingNode);
        }
        
        // Process children
        final var children = workingNode.getChildren();
        for (var child : children) {
            final var result = dispatchNode(config, commandDispatch, input, child, depth + 1);
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
            addOptionalChildren(dispatch, node);
            dispatch.setResult(CommandDispatch.Result.COMPLETE);
            return dispatch;
        }
        
        final var requiredNode = findRequiredNode(node);
        
        if (requiredNode == null) {
            dispatch.setResult(CommandDispatch.Result.COMPLETE);
            addOptionalChildren(dispatch, node);
        } else {
            dispatch.setResult(requiredNode.isCommand()
                    ? CommandDispatch.Result.COMPLETE
                    : CommandDispatch.Result.UNKNOWN);
        }
        
        return dispatch;
    }
    
    /**
     * Fast flag checking
     */
    private static boolean isFlag(String input) {
        return input.length() > 1 && input.charAt(0) == '-';
    }
    
    /**
     * Optimized required node search
     */
    private @Nullable ParameterNode<S, ?> findRequiredNode(ParameterNode<S, ?> currentNode) {
        final var children = currentNode.getChildren();
        for (var child : children) {
            if (child.isRequired()) {
                return child;
            }
            final var deepReq = findRequiredNode(child);
            if (deepReq != null) {
                return deepReq;
            }
        }
        return null;
    }
    
    /**
     * Streamlined optional children addition
     */
    private void addOptionalChildren(CommandDispatch<S> dispatch, ParameterNode<S, ?> currentNode) {
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
    
    /**
     * Fast optional child getter
     */
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
     * Optimized input matching
     */
    private static <S extends Source> boolean matchesInput(
            ParameterNode<S, ?> node,
            String input,
            boolean strictMode
    ) {
        if (node instanceof CommandNode || strictMode || node.isFlag()) {
            return node.matchesInput(input);
        }
        return true;
    }
    
    // Tab completion - optimized version without excessive profiling
    public @NotNull CompletableFuture<List<String>> tabComplete(Imperat<S> imperat, SuggestionContext<S> context) {
        final int depthToReach = context.getArgToComplete().index();
        final var arguments = context.arguments();
        final var source = context.source();
        final var permissionResolver = imperat.config().getPermissionResolver();
        final boolean ignorePerms = root.data.isIgnoringACPerms();
        final var config = imperat.config();
        
        ParameterNode<S, ?> node = root;
        for (int i = 0; i < depthToReach; i++) {
            final String raw = arguments.getOr(i, null);
            if (raw == null) {
                break;
            }
            var child = node.getChild((c) -> {
                boolean hasPerm = (ignorePerms || permissionResolver.hasPermission(source, c.data.permission()));
                boolean matches = matchesInput(c, raw, config.strictCommandTree());
                return hasPerm && matches;
            });
            if (child == null) {
                break;
            }
            node = child;
        }
        
        final var validChildren = collectValidChildren(node, source, permissionResolver, ignorePerms, config);
        
        if (validChildren.isEmpty()) {
            return CompletableFuture.completedFuture(EMPTY_STRING_LIST);
        }
        
        return processValidChildren(imperat, context, validChildren);
    }
    
    /**
     * Optimized valid children collection
     */
    private List<ParameterNode<S, ?>> collectValidChildren(
            ParameterNode<S, ?> node,
            S source,
            Object permissionResolver,
            boolean ignorePerms,
            ImperatConfig<S> config
    ) {
        final var validChildren = new ArrayList<ParameterNode<S, ?>>(8);
        final var skippedSimilarChildren = new HashSet<String>(4);
        final boolean overlapOptionalArgs = config.isOptionalParameterSuggestionOverlappingEnabled();
        
        final var children = node.getChildren();
        
        for (var child : children) {
            // Permission check
            if (!ignorePerms && !hasPermission(permissionResolver, source, child.data.permission())) {
                continue;
            }
            
            if (child.isRequired()) {
                validChildren.add(child);
                continue;
            }
            
            // For optional nodes
            if (overlapOptionalArgs || !hasSimilarNodeWithDifferentDepth(node, child)) {
                final String childFormat = child.data.format();
                if (!skippedSimilarChildren.contains(childFormat)) {
                    validChildren.add(child);
                    skippedSimilarChildren.add(childFormat);
                }
            }
        }
        
        return validChildren;
    }
    

    
    private CompletableFuture<List<String>> processValidChildren(
            Imperat<S> imperat,
            SuggestionContext<S> context,
            List<ParameterNode<S, ?>> validChildren
    ) {
        final var childrenToProcess = filterChildrenToProcess(validChildren);
        
        if (childrenToProcess.isEmpty()) {
            return CompletableFuture.completedFuture(EMPTY_STRING_LIST);
        }
        
        if (childrenToProcess.size() == 1) {
            final var child = childrenToProcess.get(0);
            final var resolver = imperat.config().getParameterSuggestionResolver(child.data);
            return resolver.asyncAutoComplete(context, child.data);
        }
        
        // Parallel processing
        final var futures = new ArrayList<CompletableFuture<List<String>>>(childrenToProcess.size());
        for (var child : childrenToProcess) {
            final var resolver = imperat.config().getParameterSuggestionResolver(child.data);
            futures.add(resolver.asyncAutoComplete(context, child.data));
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    final var result = new ArrayList<String>(64);
                    futures.forEach(future -> result.addAll(future.join()));
                    return result;
                });
    }
    
    private List<ParameterNode<S, ?>> filterChildrenToProcess(List<ParameterNode<S, ?>> validChildren) {
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
    
    private boolean hasSimilarNodeWithDifferentDepth(
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
    
    // Optimized usage search
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
    
    private Set<CommandUsage<S>> getClosestUsagesRecursively(
            Set<CommandUsage<S>> currentUsages,
            ParameterNode<S, ?> node,
            Context<S> context
    ) {
        if (node.isExecutable()) {
            final var usage = node.getExecutableUsage();
            if (hasUsagePermission(context.imperatConfig().getPermissionResolver(), context.source(), usage)) {
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
     * Type-safe permission checking
     */
    @SuppressWarnings("unchecked")
    private boolean hasPermission(Object resolver, S source, String permission) {
        if (resolver instanceof dev.velix.imperat.resolvers.PermissionResolver) {
            return ((dev.velix.imperat.resolvers.PermissionResolver<S>) resolver).hasPermission(source, permission);
        }
        return true;
    }
    
    @SuppressWarnings("unchecked")
    private boolean hasUsagePermission(Object resolver, S source, CommandUsage<S> usage) {
        if (resolver instanceof dev.velix.imperat.resolvers.PermissionResolver) {
            return ((dev.velix.imperat.resolvers.PermissionResolver<S>) resolver).hasUsagePermission(source, usage);
        }
        return true;
    }
}