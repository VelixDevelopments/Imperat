package dev.velix.imperat.command.parameters;

import dev.velix.imperat.command.Description;
import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class NormalCommandParameter<S extends Source> extends InputParameter<S> {

    NormalCommandParameter(String name,
                           ParameterType<S, ?> type,
                           @Nullable String permission,
                           Description description,
                           boolean optional,
                           boolean greedy,
                           @NotNull OptionalValueSupplier valueSupplier,
                           @Nullable SuggestionResolver<S> suggestionResolver) {
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
