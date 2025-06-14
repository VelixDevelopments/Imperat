package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.exception.parse.InvalidBooleanException;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

public final class ParameterBoolean<S extends Source> extends BaseParameterType<S, Boolean> {

    private final static Map<String, Boolean> VARIANTS = Map.of(
        "t", true, "f", false,
        "yes", true, "no", false,
        "y", true, "n", false,
        "on", true, "off", false,
        "enabled", true, "disabled", false
    );

    private boolean allowVariants = false;

    ParameterBoolean() {
        super(TypeWrap.of(Boolean.class));
        withSuggestions("true", "false");
    }

    @Override
    public @Nullable Boolean resolve(@NotNull ExecutionContext<S> context, @NotNull CommandInputStream<S> commandInputStream, String input) throws ImperatException {

        var raw = commandInputStream.currentRaw().orElse(null);
        assert raw != null;

        if (raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(raw);
        }

        if (allowVariants) {
            return VARIANTS.get(raw.toLowerCase());
        } else {
            throw new InvalidBooleanException(raw);
        }
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<S> parameter) {

        if (!allowVariants && (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false")))
            return true;
        else if (allowVariants) {
            return VARIANTS.get(input.toLowerCase(Locale.ENGLISH)) != null;
        }

        return Boolean.parseBoolean(input);
    }

    public ParameterBoolean<S> setAllowVariants(boolean allowVariants) {
        this.allowVariants = allowVariants;
        if (allowVariants) {
            suggestions.addAll(VARIANTS.keySet());
        } else {
            suggestions.removeAll(VARIANTS.keySet());
        }
        return this;
    }

    public ParameterBoolean<S> allowVariants() {
        return setAllowVariants(true);
    }
}
