package dev.velix.imperat.commands;

import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ParameterDuration<S extends Source> extends BaseParameterType<S, Duration> {

    private final SuggestionResolver<S> resolver = SuggestionResolver.plain(
            "permanent", "30d", "1y", "5y"
    );

    public ParameterDuration() {
        super();
    }

    @Override
    public @Nullable Duration resolve(
            @NotNull ExecutionContext<S> context,
            @NotNull CommandInputStream<S> cis,
            String input
    ) throws ImperatException {
        final long ms = Utilities.convertDurationToMs(input);
        if (ms == 0) {
            throw new SourceException("Bad duration input");
        }
        return new Duration(ms);
    }

    @Override
    public SuggestionResolver<S> getSuggestionResolver() {
        return resolver;
    }

}
