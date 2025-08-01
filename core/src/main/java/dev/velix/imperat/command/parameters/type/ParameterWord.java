package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.parse.WordOutOfRestrictionsException;
import dev.velix.imperat.util.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ParameterWord<S extends Source> extends BaseParameterType<S, String> {

    private final List<String> restrictions = new ArrayList<>();

    ParameterWord() {
        super();
    }

    @Override
    public @NotNull String resolve(@NotNull ExecutionContext<S> context, @NotNull CommandInputStream<S> commandInputStream, @NotNull String input) throws ImperatException {
        if (restrictions.isEmpty()) {
            return input;
        }
        if(!restrictions.contains(input)) {
            throw new WordOutOfRestrictionsException(input, restrictions);
        }
        return input;
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<S> parameter) {
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
