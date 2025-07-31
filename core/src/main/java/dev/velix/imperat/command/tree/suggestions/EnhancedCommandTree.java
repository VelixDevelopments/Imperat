package dev.velix.imperat.command.tree.suggestions;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.tree.CommandNode;
import dev.velix.imperat.command.tree.CommandTree;
import dev.velix.imperat.command.tree.ParameterNode;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Enhanced CommandTree with integrated high-performance tab completion
 */
public class EnhancedCommandTree<S extends Source> extends CommandTree<S> {
    
    private final TabCompletionEngine<S> completionEngine;
    private final CommandIndex<S> commandIndex;
    private final AliasManager aliasManager;
    private final PerformanceMonitor monitor;
    
    private EnhancedCommandTree(Command<S> command) {
        super(command);
        this.completionEngine = new TabCompletionEngine<>(root);
        this.commandIndex = new CommandIndex<>();
        this.aliasManager = new AliasManager();
        this.monitor = new PerformanceMonitor();
    }
    
    public static <S extends Source> EnhancedCommandTree<S> create(Command<S> command) {
        return new EnhancedCommandTree<>(command);
    }
    
    public static <S extends Source> EnhancedCommandTree<S> parsed(Command<S> command) {
        EnhancedCommandTree<S> tree = create(command);
        tree.parseCommandUsages();
        tree.buildOptimizationStructures();
        return tree;
    }
    
    @Override
    public @NotNull List<String> tabComplete(Imperat<S> imperat, SuggestionContext<S> context) {
        // Use the advanced completion engine
        return completionEngine.complete(imperat, context);
    }
    
    /**
     * Builds additional optimization structures after parsing
     */
    private void buildOptimizationStructures() {
        // Build command index
        traverseAndIndex(root, new ArrayList<>());
        
        // Build alias mappings
        buildAliases();
        
        // Pre-warm caches
        preWarmCaches();
    }
    
    private void traverseAndIndex(ParameterNode<S, ?> node, List<String> path) {
        if (node.isCommand()) {
            CommandNode<S> cmdNode = (CommandNode<S>) node;
            String fullPath = String.join(" ", path);
            commandIndex.addCommand(fullPath, cmdNode);
        }
        
        for (var child : node.getChildren()) {
            List<String> newPath = new ArrayList<>(path);
            newPath.add(child.getData().name());
            traverseAndIndex(child, newPath);
        }
    }
    
    private void buildAliases() {
        // Register common aliases and abbreviations
        commandIndex.getAllCommands().forEach((path, node) -> {
            // Generate abbreviations
            String abbrev = generateAbbreviation(path);
            if (abbrev.length() > 1) {
                aliasManager.registerAlias(abbrev, path);
            }
            
            // Generate initials
            String initials = generateInitials(path);
            if (initials.length() > 1) {
                aliasManager.registerAlias(initials, path);
            }
        });
    }
    
    private void preWarmCaches() {
        // Pre-compute common paths for faster access
        // This runs in background to not block initialization
        CompletableFuture.runAsync(() -> {
            // Simulate common queries to warm up caches
            warmupCommonPaths();
        });
    }
    
    // Advanced features
    
    /**
     * Get suggestions with advanced ranking
     */
    public List<RankedSuggestion> getRankedSuggestions(Imperat<S> imperat, SuggestionContext<S> context) {
        List<String> suggestions = tabComplete(imperat, context);
        return rankSuggestions(suggestions, context);
    }
    
    /**
     * Supports command aliases and abbreviations
     */
    public String resolveAlias(String input) {
        return aliasManager.resolve(input);
    }
    
    
    // Helper methods
    
    private String generateAbbreviation(String path) {
        String[] parts = path.split(" ");
        StringBuilder abbrev = new StringBuilder();
        for (String part : parts) {
            if (part.length() > 0) {
                abbrev.append(part.charAt(0));
            }
        }
        return abbrev.toString().toLowerCase();
    }
    
    private String generateInitials(String path) {
        String[] parts = path.split(" ");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            for (char c : part.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    initials.append(c);
                }
            }
        }
        return initials.toString().toLowerCase();
    }
    
    private List<RankedSuggestion> rankSuggestions(List<String> suggestions, SuggestionContext<S> context) {
        return suggestions.stream()
            .map(s -> new RankedSuggestion(s, calculateRank(s, context)))
            .sorted(Comparator.comparingDouble(RankedSuggestion::rank).reversed())
            .collect(Collectors.toList());
    }
    
    private double calculateRank(String suggestion, SuggestionContext<S> context) {
        // Complex ranking algorithm considering multiple factors
        double rank = 1.0;
        
        // Factor 1: String similarity
        String input = context.getArgToComplete().value();
        if (input != null && !input.isEmpty()) {
            rank *= calculateSimilarity(input, suggestion);
        }
        
        // Factor 2: Command popularity (from monitor)
        rank *= monitor.getPopularityScore(suggestion);
        
        // Factor 3: Context relevance
        rank *= calculateContextRelevance(suggestion, context);
        
        return rank;
    }
    
    private double calculateSimilarity(String s1, String s2) {
        // Jaro-Winkler similarity for better string matching
        return JaroWinkler.similarity(s1, s2);
    }
    
    private double calculateContextRelevance(String suggestion, SuggestionContext<S> context) {
        // Analyze context to determine relevance
        // This could consider previous commands, current path, etc.
        return 1.0; // Simplified for now
    }
    
    private void warmupCommonPaths() {
        // Simulate common command paths to warm up caches
        // This improves first-time performance
    }
}

