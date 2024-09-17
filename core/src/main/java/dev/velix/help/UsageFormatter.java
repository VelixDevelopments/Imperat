package dev.velix.help;

import dev.velix.command.Command;
import dev.velix.command.CommandUsage;
import dev.velix.context.Source;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a class responsible for formatting
 * each single {@link CommandUsage}
 */
@ApiStatus.AvailableSince("1.0.0")
public interface UsageFormatter {
    
    /**
     * Displays the usage by converting it into
     * an adventure component
     * <p>
     * This is used in the {@link PlainDisplayer}
     *
     * @param command    the command
     * @param usage      the usage to display
     * @param isLast     is it the last usage?
     * @param <S>        the sender-type
     * @return the usage component
     */
    <S extends Source> String formatUsageLine(
            Command<S> command,
            CommandUsage<S> usage,
            boolean isLast
    );
    
}
