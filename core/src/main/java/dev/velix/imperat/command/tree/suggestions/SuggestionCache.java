package dev.velix.imperat.command.tree.suggestions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LRU Cache with concurrent access support
 */
class SuggestionCache {
    private final Map<String, List<String>> cache;
    private final int maxSize;
    
    SuggestionCache(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new ConcurrentHashMap<>();
    }
    
    List<String> get(String key) {
        return cache.get(key);
    }
    
    void put(String key, List<String> value) {
        if (cache.size() >= maxSize) {
            // Simple eviction - in production use proper LRU
            cache.clear();
        }
        cache.put(key, new ArrayList<>(value));
    }
}