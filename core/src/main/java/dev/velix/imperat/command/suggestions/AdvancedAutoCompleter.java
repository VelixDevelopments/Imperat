package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;

import java.util.Collection;

final class AdvancedAutoCompleter<S extends Source> extends AutoCompleter<S> {

    AdvancedAutoCompleter(final Command<S> command) {
        super(command);
    }

    @Override
    public Collection<String> autoCompleteArgument(
            Imperat<S> dispatcher,
            SuggestionContext<S> context
    ) {
        return command.tabComplete(dispatcher, context);
    }

}
