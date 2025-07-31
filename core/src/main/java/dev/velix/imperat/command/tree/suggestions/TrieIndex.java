package dev.velix.imperat.command.tree.suggestions;

import java.util.*;

/**
 * Trie data structure for efficient prefix matching
 */
class TrieIndex {
    private final TrieNode root = new TrieNode();
    
    void insert(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new TrieNode());
        }
        current.isEndOfWord = true;
        current.word = word;
    }
    
    List<String> prefixSearch(String prefix) {
        TrieNode node = searchPrefix(prefix);
        if (node == null) {
            return Collections.emptyList();
        }
        
        List<String> results = new ArrayList<>();
        collectWords(node, results);
        return results;
    }
    
    private TrieNode searchPrefix(String prefix) {
        TrieNode current = root;
        for (char c : prefix.toCharArray()) {
            current = current.children.get(c);
            if (current == null) {
                return null;
            }
        }
        return current;
    }
    
    private void collectWords(TrieNode node, List<String> results) {
        if (node.isEndOfWord) {
            results.add(node.word);
        }
        for (TrieNode child : node.children.values()) {
            collectWords(child, results);
        }
    }
    
    static class TrieNode {
        final Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord = false;
        String word;
    }
}
