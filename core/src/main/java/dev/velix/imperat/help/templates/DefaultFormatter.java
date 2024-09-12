package dev.velix.imperat.help.templates;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.help.UsageFormatter;
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
        return "<dark_gray><bold>[<dark_aqua>+</dark_aqua>]</bold></dark_gray><green>" + format + " <white><bold>-</bold></white> <yellow>" + usage.getDescription();
    }
    
    /**
     * Formats a single syntax, this is used
     * only in the TreeDisplayer
     *
     * @param formattedUsage the format of the usage syntax
     * @return the format component of the usage
     */
    @Override
    public String formatUsageOnly(String formattedUsage) {
        return "<green>" + formattedUsage;
    }
    
}
