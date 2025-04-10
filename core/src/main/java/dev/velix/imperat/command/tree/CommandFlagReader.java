package dev.velix.imperat.command.tree;



import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.FlagData;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.Patterns;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandFlagReader<S extends Source> {

    private final TrieNode<S> root;
    private final Map<String, FlagData<S>> flagsByName;
    private final Map<Character, FlagData<S>> singleCharFlags;

    // TrieNode for efficiently matching flag aliases
    static class TrieNode<S extends Source> {
        Map<Character, TrieNode<S>> children;
        FlagData<S> flag;  // The flag this node represents (if it's a complete alias)

        public TrieNode() {
            this.children = new HashMap<>();
            this.flag = null;
        }
    }

    public CommandFlagReader(Command<S> command) {
        this.root = new TrieNode<>();
        this.flagsByName = new HashMap<>();
        this.singleCharFlags = new HashMap<>();
        for(var usage : command.usages()) {
            for(var param : usage.getParameters()) {
                if(param.isFlag()) {
                    registerFlag(param.asFlagParameter().flagData());
                }
            }
        }
        for(var freeFlag : command.getRegisteredFlags()) {
            registerFlag(freeFlag);
        }
    }

    // Register a new flag with its name and aliases
    public void registerFlag(FlagData<S> flag) {
        flagsByName.put(flag.name(), flag);

        // Add all aliases to the trie
        for (String alias : flag.aliases()) {
            if (alias.length() == 1) {
                // Store single-character aliases for quick lookup
                singleCharFlags.put(alias.charAt(0), flag);
            }

            // Add to trie
            TrieNode<S> current = root;
            for (char c : alias.toCharArray()) {
                current.children.putIfAbsent(c, new TrieNode<>());
                current = current.children.get(c);
            }
            current.flag = flag;  // Mark the end of this alias with the flag
        }
    }

    public List<FlagData<S>> parseFlags(String str) {
        return parseFlags(ArgumentQueue.parse(str));
    }

    // Parse command input and extract flags
    public List<FlagData<S>> parseFlags(ArgumentQueue queue) {
        List<FlagData<S>> result = new ArrayList<>();

        // Skip the command name
        for (int i = 1; i < queue.size(); i++) {
            String part = queue.get(i);

            if (Patterns.isInputFlag(part)) {

                String flagText = part.substring(Patterns.isDoubleFlag(part) ? 2 : 1);  // Remove the hyphen

                if (flagText.isEmpty()) continue;

                if (flagsByName.containsKey(flagText)) {
                    // Direct match with a flag name
                    result.add(flagsByName.get(flagText));
                } else if (flagText.length() == 1) {
                    // Single character flag
                    if (singleCharFlags.containsKey(flagText.charAt(0))) {
                        result.add(singleCharFlags.get(flagText.charAt(0)));
                    }
                } else if (isSingleCharCombination(flagText)) {
                    // Combined single-character flags like -abc
                    for (char c : flagText.toCharArray()) {
                        if (singleCharFlags.containsKey(c)) {
                            result.add(singleCharFlags.get(c));
                        }
                    }
                } else {
                    // Try to match combination of multi-character aliases
                    List<FlagData<S>> parsedFlags = parseMultiCharFlag(flagText);
                    result.addAll(parsedFlags);
                }
            }
        }

        return result;
    }

    // Check if this flag text consists only of single character aliases
    private boolean isSingleCharCombination(String flagText) {
        // Check if all characters are registered single-char flags
        for (char c : flagText.toCharArray()) {
            if (!singleCharFlags.containsKey(c)) {
                return false;
            }
        }
        return true;
    }

    // Parse multi-character flag combinations (like "becl" → ["be", "cl"])
    private List<FlagData<S>> parseMultiCharFlag(String flagText) {
        List<FlagData<S>> result = new ArrayList<>();
        int startIndex = 0;

        while (startIndex < flagText.length()) {
            Pair<FlagData<S>, Integer> match = findLongestMatch(flagText, startIndex);
            if (match != null) {
                result.add(match.first);
                startIndex = match.second;
            } else {
                // If no match found, skip this character
                startIndex++;
            }
        }

        return result;
    }

    // Find the longest matching alias starting at the given index
    private Pair<FlagData<S>, Integer> findLongestMatch(String text, int startIndex) {
        TrieNode<S> current = root;
        FlagData<S> lastMatchedFlag = null;
        int lastMatchedEndIndex = -1;

        for (int i = startIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!current.children.containsKey(c)) {
                break;
            }

            current = current.children.get(c);
            if (current.flag != null) {
                // Found a complete alias
                lastMatchedFlag = current.flag;
                lastMatchedEndIndex = i + 1;
            }
        }

        if (lastMatchedFlag != null) {
            return new Pair<>(lastMatchedFlag, lastMatchedEndIndex);
        }
        return null;
    }

    // Simple pair class for return values
    static class Pair<A, B> {
        A first;
        B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }
    }

}
