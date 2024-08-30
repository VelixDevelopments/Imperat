package dev.velix.imperat.help;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

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
     * @param dispatcher the dispatcher
     * @param command    the command
     * @param usage      the usage to display
     * @param isLast     is it the last usage?
     * @param <C>        the sender-type
     * @return the usage component
     */
    <C> String formatUsageLine(
            @NotNull Imperat<C> dispatcher,
            Command<C> command,
            CommandUsage<C> usage,
            boolean isLast
    );

    /**
     * Formats a single syntax, this is used
     * only in the {@link TreeDisplayer}
     *
     * @param formattedUsage the format of the usage syntax
     * @return the format component of the usage
     */
    String formatUsageOnly(String formattedUsage);
}
