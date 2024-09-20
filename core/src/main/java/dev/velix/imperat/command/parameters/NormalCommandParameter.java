package dev.velix.imperat.command.parameters;

import dev.velix.imperat.command.Description;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.StringUtils;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.Nullable;

class NormalCommandParameter<S extends Source> extends InputParameter<S> {
    
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
