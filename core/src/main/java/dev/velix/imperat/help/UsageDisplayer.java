package dev.velix.imperat.help;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.Source;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Represents how all usages together are organized to be
 * displayed to the command sender.
 * <p>
 * This class is only responsible for organizing the
 * formatted usages, NOT for formatting the usages
 * <p>
 * Formatting the usages is the mere responsibility
 * of the class {@link UsageFormatter}
 */
@ApiStatus.AvailableSince("1.0.0")
@FunctionalInterface
public interface UsageDisplayer {

    /**
     * Displays all usages together to
     * the {@link Source}
     * <p>
     * It has two pre-made implementations
     *
     * @param dispatcher the command dispatcher
     * @param command    the command that owns all the usages
     * @param source     the command sender
     * @param formatter  the formatter {@link UsageFormatter}
     * @param usages     the usages to display
     * @param <C>        the command-sender type
     * @see PlainDisplayer
     * @see TreeDisplayer
     */
    <C> void display(
            Imperat<C> dispatcher,
            Command<C> command,
            Source<C> source,
            UsageFormatter formatter,
            List<CommandUsage<C>> usages
    );

    static UsageDisplayer plain() {
        return new PlainDisplayer();
    }

    static UsageDisplayer tree() {
        return new TreeDisplayer();
    }

    static UsageDisplayer custom(UsageDisplayer displayer) {
        return displayer;
    }
}
