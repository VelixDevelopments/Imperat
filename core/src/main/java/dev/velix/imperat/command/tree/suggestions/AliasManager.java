package dev.velix.imperat.command.tree.suggestions;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages command aliases and abbreviations
 */
class AliasManager {
    private final Map<String, String> aliasToCommand = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> commandToAliases = new ConcurrentHashMap<>();
    
    void registerAlias(String alias, String command) {
        aliasToCommand.put(alias, command);
        commandToAliases.computeIfAbsent(command, k -> ConcurrentHashMap.newKeySet()).add(alias);
    }
    
    String resolve(String input) {
        return aliasToCommand.getOrDefault(input, input);
    }
    
    Set<String> getAliases(String command) {
        return commandToAliases.getOrDefault(command, Collections.emptySet());
    }
}
