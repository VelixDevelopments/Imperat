package dev.velix.imperat.command.tree.suggestions;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.tree.CommandNode;
import dev.velix.imperat.command.tree.ParameterNode;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * High-performance tab completion engine with multiple optimization layers
 */
public class TabCompletionEngine<S extends Source> {
    
    // Core components
    private final CommandNode<S> root;
    private final SuggestionCache cache;
    private final FuzzyMatcher fuzzyMatcher;
    private final TrieIndex trieIndex;
    private final UsageTracker usageTracker;
    private final ExecutorService executorService;
    
    // Configuration
    private static final int MAX_SUGGESTIONS = 20;
    private static final int CACHE_SIZE = 10000;
    private static final double FUZZY_THRESHOLD = 0.7;
    
    public TabCompletionEngine(CommandNode<S> root) {
        this.root = root;
        this.cache = new SuggestionCache(CACHE_SIZE);
        this.fuzzyMatcher = new FuzzyMatcher(FUZZY_THRESHOLD);
        this.trieIndex = new TrieIndex();
        this.usageTracker = new UsageTracker();
        this.executorService = ForkJoinPool.commonPool();
        
        // Build indices
        buildIndices();
    }
    
    /**
     * Main entry point for tab completion
     */
    public List<String> complete(Imperat<S> imperat, SuggestionContext<S> context) {
        String cacheKey = buildCacheKey(context);
        
        // L1 Cache check
        List<String> cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Parallel completion strategy
        CompletionRequest<S> request = new CompletionRequest<>(imperat, context);
        List<ScoredSuggestion> suggestions = performCompletion(request);
        
        // Sort by score and extract strings
        List<String> results = suggestions.stream()
            .sorted(Comparator.reverseOrder())
            .limit(MAX_SUGGESTIONS)
            .map(s -> s.suggestion)
            .collect(Collectors.toList());
        
        // Cache results
        cache.put(cacheKey, results);
        
        // Track usage for learning
        if (!results.isEmpty()) {
            usageTracker.recordQuery(context.getArgToComplete().value(), results.get(0));
        }
        
        return results;
    }
    
    /**
     * Performs multi-strategy completion
     */
    private List<ScoredSuggestion> performCompletion(CompletionRequest<S> request) {
        List<CompletableFuture<List<ScoredSuggestion>>> futures = new ArrayList<>();
        
        // Strategy 1: Exact prefix matching (highest priority)
        futures.add(CompletableFuture.supplyAsync(() -> 
            exactPrefixMatch(request), executorService));
        
        // Strategy 2: Fuzzy matching
        futures.add(CompletableFuture.supplyAsync(() -> 
            fuzzyMatch(request), executorService));
        
        // Strategy 3: Contextual suggestions
        futures.add(CompletableFuture.supplyAsync(() -> 
            contextualSuggestions(request), executorService));
        
        // Strategy 4: Trie-based matching
        futures.add(CompletableFuture.supplyAsync(() -> 
            trieMatch(request), executorService));
        
        // Combine results
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList()))
            .join();
    }
    
    /**
     * Strategy 1: Exact prefix matching with optimized traversal
     */
    private List<ScoredSuggestion> exactPrefixMatch(CompletionRequest<S> request) {
        List<ScoredSuggestion> results = new ArrayList<>();
        String prefix = request.context.getArgToComplete().value();
        int targetDepth = request.context.getArgToComplete().index();
        
        // Use BFS for level-order traversal
        Queue<ParameterNode<S, ?>> queue = new LinkedList<>();
        queue.offer(root);
        
        while (!queue.isEmpty()) {
            ParameterNode<S, ?> node = queue.poll();
            
            if (node.getDepth() == targetDepth - 1) {
                // We're at the parent level of our target
                for (var child : node.getChildren()) {
                    if (hasPermission(request, child)) {
                        var suggestions = getSuggestionsForNode(request, child);
                        for (String suggestion : suggestions) {
                            if (prefix == null || prefix.isEmpty() || suggestion.startsWith(prefix)) {
                                double score = calculateScore(suggestion, prefix, child);
                                results.add(new ScoredSuggestion(suggestion, score));
                            }
                        }
                    }
                }
            } else if (node.getDepth() < targetDepth - 1) {
                // Continue traversal
                for (var child : node.getChildren()) {
                    if (matchesPath(request, child)) {
                        queue.offer(child);
                    }
                }
            }
        }
        
        return results;
    }
    
    /**
     * Strategy 2: Fuzzy matching for typo tolerance
     */
    private List<ScoredSuggestion> fuzzyMatch(CompletionRequest<S> request) {
        String input = request.context.getArgToComplete().value();
        if (input == null || input.length() < 2) {
            return Collections.emptyList();
        }
        
        return fuzzyMatcher.findMatches(input, getAllPossibleSuggestions(request));
    }
    
    /**
     * Strategy 3: Context-aware suggestions based on usage patterns
     */
    private List<ScoredSuggestion> contextualSuggestions(CompletionRequest<S> request) {
        List<String> previousArgs = request.context.arguments().asImmutableCopy();
        return usageTracker.getContextualSuggestions(previousArgs, request.context.getArgToComplete().index());
    }
    
    /**
     * Strategy 4: Trie-based prefix matching
     */
    private List<ScoredSuggestion> trieMatch(CompletionRequest<S> request) {
        String prefix = request.context.getArgToComplete().value();
        if (prefix == null || prefix.isEmpty()) {
            return Collections.emptyList();
        }
        
        return trieIndex.prefixSearch(prefix).stream()
            .map(s -> new ScoredSuggestion(s, 0.8))
            .collect(Collectors.toList());
    }
    
    /**
     * Builds indices for fast lookups
     */
    private void buildIndices() {
        // Build trie index
        traverseTree(root, (node, path) -> {
            if (node instanceof CommandNode) {
                trieIndex.insert(node.getData().name());
            }
        });
    }
    
    /**
     * Helper method to traverse the tree
     */
    private void traverseTree(ParameterNode<S, ?> node, BiConsumer<ParameterNode<S, ?>, List<ParameterNode<S, ?>>> visitor) {
        List<ParameterNode<S, ?>> path = new ArrayList<>();
        traverseTreeRecursive(node, path, visitor);
    }
    
    private void traverseTreeRecursive(ParameterNode<S, ?> node, List<ParameterNode<S, ?>> path, 
                                      BiConsumer<ParameterNode<S, ?>, List<ParameterNode<S, ?>>> visitor) {
        path.add(node);
        visitor.accept(node, Collections.unmodifiableList(path));
        
        for (var child : node.getChildren()) {
            traverseTreeRecursive(child, path, visitor);
        }
        
        path.remove(path.size() - 1);
    }
    
    private double calculateScore(String suggestion, String prefix, ParameterNode<S, ?> node) {
        double score = 1.0;
        
        // Exact match bonus
        if (suggestion.equalsIgnoreCase(prefix)) {
            score += 0.5;
        }
        
        // Usage frequency bonus
        score += usageTracker.getFrequencyScore(suggestion) * 0.3;
        
        // Command node bonus (commands are usually more important)
        if (node instanceof CommandNode) {
            score += 0.2;
        }
        
        // Length penalty (shorter suggestions are often better)
        score -= (suggestion.length() / 100.0) * 0.1;
        
        return score;
    }
    
    private String buildCacheKey(SuggestionContext<S> context) {
        return context.arguments().asImmutableView() +
               "|" + context.getArgToComplete().index() + 
               "|" + context.getArgToComplete().value();
    }
    
    // Helper classes
    
    record ScoredSuggestion(String suggestion, double score) implements Comparable<ScoredSuggestion> {
        
        @Override
        public int compareTo(ScoredSuggestion other) {
            return Double.compare(this.score, other.score);
        }
        
    }
    
    record CompletionRequest<S extends Source>(Imperat<S> imperat, SuggestionContext<S> context) { }
    
    // Utility methods
    
    private boolean hasPermission(CompletionRequest<S> request, ParameterNode<S, ?> node) {
        return request.imperat.config().getPermissionResolver()
            .hasPermission(request.context.source(), node.getData().permission());
    }
    
    private boolean matchesPath(CompletionRequest<S> request, ParameterNode<S, ?> node) {
        String arg = request.context.arguments().getOr(node.getDepth(), null);
        return arg == null || node.matchesInput(arg);
    }
    
    private List<String> getSuggestionsForNode(CompletionRequest<S> request, ParameterNode<S, ?> node) {
        var resolver = request.imperat.config().getParameterSuggestionResolver(node.getData());
        return resolver.autoComplete(request.context, node.getData());
    }
    
    private Set<String> getAllPossibleSuggestions(CompletionRequest<S> request) {
        // This would collect all possible suggestions at the current depth
        // Implementation depends on your specific needs
        return new HashSet<>();
    }
}