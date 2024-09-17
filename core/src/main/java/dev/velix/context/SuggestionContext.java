package dev.velix.context;

import dev.velix.command.suggestions.CompletionArg;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the context for auto-completion while providing suggestions
 *
 * @param <S> the command-source
 */
public interface SuggestionContext<S extends Source> extends Context<S> {
    
    /**
     * @return The info about the argument being completed
     */
    @NotNull CompletionArg getArgToComplete();
    
    
}
