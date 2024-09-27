package dev.velix.imperat.context.internal;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public record ResolvedArgument<S extends Source>(
        @Nullable String raw,
        CommandParameter<S> parameter,
        int index, @Nullable Object value
) {
    @Override
    public String toString() {
        return "ResolvedArgument{" +
                "raw='" + raw + '\'' +
                ", parameter=" + parameter.format() +
                ", index=" + index +
                ", value=" + value +
                '}';
    }
}
