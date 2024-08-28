package dev.velix.imperat.command.parameters;

import dev.velix.imperat.command.Description;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.StringUtils;

class NormalCommandParameter extends InputParameter {

    NormalCommandParameter(String name,
                           Class<?> type,
                           Description description,
                           boolean optional,
                           boolean greedy,
                           OptionalValueSupplier<?> valueSupplier,
                           SuggestionResolver<?, ?> suggestionResolver) {
        super(name, type, description, optional, false, greedy, valueSupplier, suggestionResolver);
    }
    
    NormalCommandParameter(String name,
                           Class<?> type,
                           boolean optional,
                           boolean greedy,
                           OptionalValueSupplier<?> valueSupplier,
                           SuggestionResolver<?, ?> suggestionResolver) {
        super(name, type, Description.EMPTY, optional, false, greedy, valueSupplier, suggestionResolver);
    }
    
    /**
     * Formats the usage parameter
     *
     * @return the formatted parameter
     */
    @Override
    public String format() {
        var content = getName();
        if (isGreedy())
            content += "...";
        return StringUtils.normalizedParameterFormatting(content, isOptional());
    }
}