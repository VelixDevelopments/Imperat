package dev.velix.context.internal;

import dev.velix.Imperat;
import dev.velix.command.Command;
import dev.velix.command.suggestions.CompletionArg;
import dev.velix.context.ArgumentQueue;
import dev.velix.context.Source;
import dev.velix.context.SuggestionContext;
import org.jetbrains.annotations.NotNull;

final class SuggestionContextImpl<S extends Source> extends ContextImpl<S> implements SuggestionContext<S> {
    private final CompletionArg completionArg;
    
    SuggestionContextImpl(
            Imperat<S> dispatcher,
            Command<S> command,
            S source,
            ArgumentQueue args,
            CompletionArg completionArg
    ) {
        super(dispatcher, command, source, args);
        this.completionArg = completionArg;
    }
    
    @Override
    public @NotNull CompletionArg getArgToComplete() {
        return completionArg;
    }
    
}
