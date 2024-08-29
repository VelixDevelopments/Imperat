package dev.velix.imperat.help;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.Source;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
final class PlainDisplayer implements UsageDisplayer {

    PlainDisplayer() {

    }

    @Override
    public <C> void display(Imperat<C> dispatcher,
                            Command<C> command,
                            Source<C> source,
                            UsageFormatter formatter,
                            List<CommandUsage<C>> commandUsages) {

        for (int i = 0; i < commandUsages.size(); i++) {
            var usage = commandUsages.get(i);
            var comp = formatter.formatUsageLine(dispatcher, command, usage, i == commandUsages.size() - 1);
            source.reply(comp);
        }
    }

}
