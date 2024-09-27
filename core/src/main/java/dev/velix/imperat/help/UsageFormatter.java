package dev.velix.imperat.help;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Source;
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
     *
     * @param command the command
     * @param usage   the usage to display
     * @param index   the index of the usage
     * @param <S>     the sender-type
     * @return the usage component
     */
    <S extends Source> String format(
            Command<S> command,
            CommandUsage<S> usage,
            int index
    );
    
}
