package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.sur.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.util.Preconditions;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class ParameterWord<S extends Source> extends BaseParameterType<S, String> {

    private final List<String> restrictions = new ArrayList<>();

    ParameterWord() {
        super(TypeWrap.of(String.class));
    }

    @Override
    public @Nullable String resolve(ResolvedContext<S> context, @NotNull CommandInputStream<S> commandInputStream) throws ImperatException {
        var nextRaw = commandInputStream.currentRaw();
        if (restrictions.isEmpty()) {
            return nextRaw;
        }
        return Optional.of(nextRaw).filter(restrictions::contains)
            .orElseThrow(() -> new SourceException("Word '%s' is not within the given restrictions=%s", nextRaw, restrictions.toString()));
    }

    @Override
    public Collection<String> suggestions() {
        return restrictions;
    }

    @Override
    public boolean matchesInput(String input) {
        if (!restrictions.isEmpty()) {
            return restrictions.contains(input);
        }
        return true;
    }

    public ParameterWord<S> withRestriction(String restriction) {
        Preconditions.notNull(restriction, "not null");
        restrictions.add(restriction);
        return this;
    }

    public List<String> getRestrictions() {
        return restrictions;
    }

}
