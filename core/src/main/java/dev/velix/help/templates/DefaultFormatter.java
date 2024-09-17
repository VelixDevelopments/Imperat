package dev.velix.help.templates;

import dev.velix.command.Command;
import dev.velix.command.CommandUsage;
import dev.velix.context.Source;
import dev.velix.help.UsageFormatter;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class DefaultFormatter implements UsageFormatter {
    
    
    /**
     * Displays the usage by converting it into
     * an adventure component
     * <p>
     * This is used in the PlainDisplayer
     *
     * @param command    the command
     * @param usage      the usage to display
     * @param isLast     is it the last usage?
     * @return the usage component
     */
    @Override
    public <S extends Source> String formatUsageLine(Command<S> command, CommandUsage<S> usage, boolean isLast) {
        String format = "/" + CommandUsage.format(command, usage);
        return "&8&l[&3+&8]&r &a" + format + " &r&l-&r&e " + usage.description();
    }
    
    
}
