package dev.velix.imperat.help;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.Pair;
import dev.velix.imperat.util.api.Unstable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.Internal
@ApiStatus.Experimental
@Unstable
final class TreeDisplayer implements UsageDisplayer {
    
    private final static String BRANCH_DOWN = "<dark_gray>┠</dark_gray>";
    private final static String BRANCH_FORWARDS = "<dark_gray>-</dark_gray>";
    
    TreeDisplayer() {
    
    }
    
    @Override
    public <S extends Source> void display(
            Command<S> command,
            S source,
            UsageFormatter formatter,
            List<CommandUsage<S>> usages
    ) {
        
        
        for (int i = 0; i < usages.size(); i++) {
            var usage = usages.get(i);
            
            var formatInfo = formatUsageTillSub(command, usage);
            
            var lastSub = formatInfo.right();
            
            displayUsage(command, source, usage, lastSub,
                    formatter, formatInfo.left(), i == usages.size() - 1);
        }
        
    }
    
    
    private <S extends Source> Pair<String, Command<S>> formatUsageTillSub(Command<S> command,
                                                                           CommandUsage<S> usage) {
        StringBuilder builder = new StringBuilder("/" + command.getName());
        Command<S> lastSub = null;
        for (var param : usage.getParameters()) {
            builder.append(' ')
                    .append(param.format());
            if (param.isCommand()) {
                lastSub = param.asCommand();
                break;
            }
        }
        
        return new Pair<>(builder.toString(), lastSub);
    }
    
    
    private <S extends Source> void displayUsage(
            Command<S> command,
            S source,
            CommandUsage<S> usage,
            @Nullable Command<S> sub,
            UsageFormatter formatter,
            @NotNull String format,
            boolean last
    ) {
        
        
        if (sub == null) {
            source.reply(formatter.formatUsageLine(command, usage, last));
            return;
        }
        
        source.reply(formatter.formatUsageOnly(format));
        
        int max = sub.getUsages().size();
        int i = 0;
        for (CommandUsage<S> commandUsage : sub.getUsages()) {
            var formatInfo = formatUsageTillSub(command, commandUsage);
            source.reply(BRANCH_DOWN + BRANCH_FORWARDS +
                    formatter.formatUsageOnly(formatInfo.left())
            );
            if (commandUsage.hasParamType(Command.class)) {
                displayUsage(command, source, commandUsage,
                        formatInfo.right(), formatter, formatInfo.left(), i == max - 1);
            }
            i++;
        }
        
    }
    
}
