package dev.velix.imperat.misc;

import dev.velix.imperat.components.TestSource;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.commands.annotations.examples.Group;
import dev.velix.imperat.commands.annotations.examples.GroupRegistry;
import dev.velix.imperat.commands.annotations.examples.GroupSuggestionResolver;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ParameterGroup extends BaseParameterType<TestSource, Group> {
    private final GroupSuggestionResolver suggestionResolver = new GroupSuggestionResolver();

    public ParameterGroup() {
        super();
        //static plain suggestions
    }

    @Override
    public @Nullable Group resolve(
            @NotNull ExecutionContext<TestSource> context,
            @NotNull CommandInputStream<TestSource> commandInputStream,
            String input) throws ImperatException {
        String raw = commandInputStream.currentRaw().orElse(null);
        if (raw == null) {
            return null;
        }
        return GroupRegistry.getInstance().getData(raw)
            .orElseThrow(() -> new SourceException("Unknown group '%s'", raw));
    }

    @Override
    public SuggestionResolver<TestSource> getSuggestionResolver() {
        return suggestionResolver;
    }

}
