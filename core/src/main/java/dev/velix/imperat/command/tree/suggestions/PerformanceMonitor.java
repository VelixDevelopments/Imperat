package dev.velix.imperat.command.tree.suggestions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance monitoring and analytics
 */
public class PerformanceMonitor {
    private final AtomicLong totalCompletions = new AtomicLong();
    private final AtomicLong totalTime = new AtomicLong();
    private final Map<String, AtomicLong> commandUsage = new ConcurrentHashMap<>();
    
    void recordCompletion(long nanos) {
        totalCompletions.incrementAndGet();
        totalTime.addAndGet(nanos);
    }
    
    void recordCommandUsage(String command) {
        commandUsage.computeIfAbsent(command, k -> new AtomicLong()).incrementAndGet();
    }
    
    double getPopularityScore(String command) {
        AtomicLong usage = commandUsage.get(command);
        if (usage == null) return 0.1;
        
        long totalUsage = commandUsage.values().stream()
            .mapToLong(AtomicLong::get)
            .sum();
        
        if (totalUsage == 0) return 0.1;
        
        return (double) usage.get() / totalUsage;
    }
}