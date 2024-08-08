package dev.velix.imperat.context.internal;

import dev.velix.imperat.command.parameters.UsageParameter;
import lombok.Data;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@Data
public final class ResolvedArgument {

	private final @Nullable String raw;
	private final UsageParameter parameter;
	private final int index;
	private final @Nullable Object value;

}
