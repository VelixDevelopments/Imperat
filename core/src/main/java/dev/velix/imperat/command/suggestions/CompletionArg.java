package dev.velix.imperat.command.suggestions;

import lombok.Data;

@Data
public final class CompletionArg {
	private final String arg;
	private final int index;
}
