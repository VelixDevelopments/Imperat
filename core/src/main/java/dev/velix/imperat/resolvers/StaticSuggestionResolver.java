package dev.velix.imperat.resolvers;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import java.util.List;

final class StaticSuggestionResolver<S extends Source> implements SuggestionResolver<S> {
    private final List<String> suggestions;
    
    StaticSuggestionResolver(List<String> suggestions) {
        this.suggestions = suggestions;
    }
    
    
    @Override
    public List<String> autoComplete(
            SuggestionContext<S> context,
            CommandParameter<S> parameter
    ) {
        return suggestions;
    }
}
