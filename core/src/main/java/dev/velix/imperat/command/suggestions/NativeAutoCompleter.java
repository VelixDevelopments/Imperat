package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

final class NativeAutoCompleter<S extends Source> extends AutoCompleter<S> {

    NativeAutoCompleter(Command<S> command) {
        super(command);
    }

    /**
     * Autocompletes an argument from the whole position of the
     * argument-raw input
     *
     * @param imperat the command dispatcher
     * @param context the context for suggestions
     * @return the auto-completed results
     */
    @Override
    public CompletableFuture<Collection<String>> autoComplete(Imperat<S> imperat, SuggestionContext<S> context) {
        var tree = command.tree();
        return tree.tabComplete(imperat, context);
    }


}
