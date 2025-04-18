package dev.velix.imperat.context.internal;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.suggestions.CompletionArg;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import org.jetbrains.annotations.NotNull;

final class SuggestionContextImpl<S extends Source> extends ContextImpl<S> implements SuggestionContext<S> {
    private final CompletionArg completionArg;

    SuggestionContextImpl(
        Imperat<S> dispatcher,
        Command<S> command,
        S source,
        String label,
        ArgumentQueue args,
        CompletionArg completionArg
    ) {
        super(dispatcher, command, source, label, args);
        this.completionArg = completionArg;
    }

    @Override
    public @NotNull CompletionArg getArgToComplete() {
        return completionArg;
    }

}
