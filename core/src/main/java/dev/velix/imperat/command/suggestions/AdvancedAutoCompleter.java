package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Source;

import java.util.List;

final class AdvancedAutoCompleter<S extends Source> extends AutoCompleter<S> {
    
    
    AdvancedAutoCompleter(Command<S> command) {
        super(command);
    }
    
    
    @Override
    public List<String> autoCompleteArgument(
            Imperat<S> dispatcher,
            S source,
            CompletionArg currentArg,
            String[] args
    ) {
        AutoCompleteList list = new AutoCompleteList();
        list.addAll(command.tabComplete(dispatcher, source, currentArg, args));
        return list.asList();
    }
}
