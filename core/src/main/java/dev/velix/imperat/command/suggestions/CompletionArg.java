package dev.velix.imperat.command.suggestions;

import org.jetbrains.annotations.ApiStatus;

/**
 * Represents an argument that's being completed
 * or an argument that is requested by the command-sender
 * to be completed
 *
 * @param arg   the argument input half-complete or empty to be completed
 * @param index the index of this argument
 */
@ApiStatus.AvailableSince("1.0.0")
public record CompletionArg(String arg, int index) {

}
