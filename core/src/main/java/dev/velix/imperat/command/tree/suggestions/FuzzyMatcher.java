package dev.velix.imperat.command.tree.suggestions;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fuzzy matching implementation using Levenshtein distance
 */
class FuzzyMatcher {
    private final double threshold;
    
    FuzzyMatcher(double threshold) {
        this.threshold = threshold;
    }
    
    List<TabCompletionEngine.ScoredSuggestion> findMatches(String input, Set<String> candidates) {
        return candidates.parallelStream()
            .map(candidate -> {
                double similarity = calculateSimilarity(input, candidate);
                return new TabCompletionEngine.ScoredSuggestion(candidate, similarity);
            })
            .filter(s -> s.score() >= threshold)
            .collect(Collectors.toList());
    }
    
    private double calculateSimilarity(String s1, String s2) {
        int distance = levenshteinDistance(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());
        return 1.0 - (double) distance / maxLen;
    }
    
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], 
                              Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
}