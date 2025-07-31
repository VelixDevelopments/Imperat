package dev.velix.imperat.command.tree.suggestions;

import dev.velix.imperat.command.tree.CommandNode;
import dev.velix.imperat.context.Source;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Command indexing for O(1) lookups
 */
class CommandIndex<S extends Source> {
    private final Map<String, CommandNode<S>> pathIndex = new ConcurrentHashMap<>();
    private final Map<Integer, Set<CommandNode<S>>> depthIndex = new ConcurrentHashMap<>();
    
    void addCommand(String path, CommandNode<S> node) {
        pathIndex.put(path, node);
        depthIndex.computeIfAbsent(node.getDepth(), k -> ConcurrentHashMap.newKeySet()).add(node);
    }
    
    CommandNode<S> getByPath(String path) {
        return pathIndex.get(path);
    }
    
    Set<CommandNode<S>> getByDepth(int depth) {
        return depthIndex.getOrDefault(depth, Collections.emptySet());
    }
    
    Map<String, CommandNode<S>> getAllCommands() {
        return Collections.unmodifiableMap(pathIndex);
    }
}
