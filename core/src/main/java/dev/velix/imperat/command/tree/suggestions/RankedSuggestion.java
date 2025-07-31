package dev.velix.imperat.command.tree.suggestions;

/**
 * Ranked suggestion with score
 */
public record RankedSuggestion(String suggestion, double rank) {
}
