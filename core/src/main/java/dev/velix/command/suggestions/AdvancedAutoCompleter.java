package dev.velix.command.suggestions;

import dev.velix.Imperat;
import dev.velix.command.Command;
import dev.velix.context.Source;
import dev.velix.context.SuggestionContext;

import java.util.List;

final class AdvancedAutoCompleter<S extends Source> extends AutoCompleter<S> {
    
    
    AdvancedAutoCompleter(Command<S> command) {
        super(command);
    }
    
    
    @Override
    public List<String> autoCompleteArgument(
            Imperat<S> dispatcher,
            SuggestionContext<S> context
    ) {
        AutoCompleteList list = new AutoCompleteList();
        list.addAll(command.tabComplete(dispatcher, context));
        return list.asList();
    }
}
