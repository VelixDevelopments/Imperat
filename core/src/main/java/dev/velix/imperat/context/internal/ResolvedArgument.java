package dev.velix.imperat.context.internal;

import dev.velix.imperat.command.parameters.UsageParameter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public record ResolvedArgument(@Nullable String raw, UsageParameter parameter,
                               int index, @Nullable Object value) {

}
