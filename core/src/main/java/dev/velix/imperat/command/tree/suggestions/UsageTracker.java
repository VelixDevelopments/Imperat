package dev.velix.imperat.command.tree.suggestions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * Tracks usage patterns for intelligent suggestions
 */
class UsageTracker {
    private final Map<String, LongAdder> commandFrequency = new ConcurrentHashMap<>();
    private final Map<String, Map<String, AtomicInteger>> contextualPatterns = new ConcurrentHashMap<>();
    
    void recordQuery(String query, String selectedSuggestion) {
        commandFrequency.computeIfAbsent(selectedSuggestion, k -> new LongAdder()).increment();
    }
    
    double getFrequencyScore(String command) {
        LongAdder adder = commandFrequency.get(command);
        if (adder == null) return 0.0;
        
        long count = adder.sum();
        // Logarithmic scaling to prevent dominant commands
        return Math.log(count + 1) / 10.0;
    }
    
    List<TabCompletionEngine.ScoredSuggestion> getContextualSuggestions(List<String> context, int depth) {
        // Implementation would analyze patterns based on context
        return Collections.emptyList();
    }
}