package dev.velix.imperat.examples.help;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.help.UsageFormatter;

public class ExampleUsageFormatter implements UsageFormatter {
    
    
    @Override
    public <S extends Source> String formatUsageLine(Command<S> command, CommandUsage<S> usage, boolean isLast) {
        String format = "/" + CommandUsage.format(command, usage);
        return "&a" + format + " &r&l-&r &e<yellow>" + usage.description();
    }
    
}
