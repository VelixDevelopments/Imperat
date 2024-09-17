package dev.velix.command.parameters;

import dev.velix.command.Description;
import dev.velix.resolvers.SuggestionResolver;
import dev.velix.supplier.OptionalValueSupplier;
import dev.velix.util.StringUtils;
import dev.velix.util.TypeWrap;
import org.jetbrains.annotations.Nullable;

class NormalCommandParameter extends InputParameter {
    
    NormalCommandParameter(String name,
                           TypeWrap<?> type,
                           @Nullable String permission,
                           Description description,
                           boolean optional,
                           boolean greedy,
                           OptionalValueSupplier<?> valueSupplier,
                           SuggestionResolver<?, ?> suggestionResolver) {
        super(
                name, type, permission, description, optional,
                false, greedy, valueSupplier, suggestionResolver
        );
    }
    
    /**
     * Formats the usage parameter
     *
     * @return the formatted parameter
     */
    @Override
    public String format() {
        var content = name();
        if (isGreedy())
            content += "...";
        return StringUtils.normalizedParameterFormatting(content, isOptional());
    }
}
