package dev.velix.imperat.help;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
final class PlainDisplayer implements UsageDisplayer {
    
    PlainDisplayer() {
    
    }
    
    @Override
    public <S extends Source> void display(Command<S> command,
                                           S source,
                                           UsageFormatter formatter,
                                           List<CommandUsage<S>> commandUsages) {
        
        for (int i = 0; i < commandUsages.size(); i++) {
            var usage = commandUsages.get(i);
            String comp = formatter.formatUsageLine(command, usage, i == commandUsages.size() - 1);
            source.reply(comp);
        }
    }
    
}
