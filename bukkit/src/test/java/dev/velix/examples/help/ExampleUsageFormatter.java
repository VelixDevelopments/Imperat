package dev.velix.examples.help;

import dev.velix.command.Command;
import dev.velix.command.CommandUsage;
import dev.velix.context.Source;
import dev.velix.help.UsageFormatter;

public class ExampleUsageFormatter implements UsageFormatter {
    
    
    @Override
    public <S extends Source> String formatUsageLine(Command<S> command, CommandUsage<S> usage, boolean isLast) {
        String format = "/" + CommandUsage.format(command, usage);
        return "&a" + format + " &r&l-&r &e<yellow>" + usage.description();
    }
    
}
