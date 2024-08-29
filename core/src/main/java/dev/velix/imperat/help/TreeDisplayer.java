package dev.velix.imperat.help;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.Source;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.util.Pair;
import dev.velix.imperat.util.api.Unstable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.Internal
@ApiStatus.Experimental
@Unstable
final class TreeDisplayer implements UsageDisplayer {

    private final static Component BRANCH_DOWN = Component.text("┠", NamedTextColor.DARK_GRAY);
    private final static Component BRANCH_FORWARDS = Component.text("-", NamedTextColor.DARK_GRAY);

    TreeDisplayer() {

    }

    @Override
    public <C> void display(
            Imperat<C> dispatcher,
            Command<C> command,
            Source<C> source,
            UsageFormatter formatter,
            List<CommandUsage<C>> usages
    ) {


        for (int i = 0; i < usages.size(); i++) {
            var usage = usages.get(i);

            var formatInfo = formatUsageTillSub(dispatcher, command, usage);

            var lastSub = formatInfo.left();

            displayUsage(dispatcher, command, source, usage, lastSub,
                    formatter, formatInfo.right(), i == usages.size() - 1);
        }

    }


    @SuppressWarnings("unchecked")
    private <C> Pair<String, Command<C>> formatUsageTillSub(Imperat<C> dispatcher,
                                                            Command<C> command,
                                                            CommandUsage<C> usage) {
        StringBuilder builder = new StringBuilder(dispatcher.commandPrefix() + command.getName());
        Command<C> lastSub = null;
        for (var param : usage.getParameters()) {
            builder.append(' ')
                    .append(param.format());
            if (param.isCommand()) {
                lastSub = (Command<C>) param.asCommand();
                break;
            }
        }

        return new Pair<>(builder.toString(), lastSub);
    }


    private <C> void displayUsage(
            Imperat<C> dispatcher,
            Command<C> command,
            Source<C> source,
            CommandUsage<C> usage,
            @Nullable Command<C> sub,
            UsageFormatter formatter,
            @NotNull String format,
            boolean last
    ) {


        if (sub == null) {
            source.reply(formatter.formatUsageLine(dispatcher, command, usage, last));
            return;
        }

        source.reply(formatter.formatUsageOnly(format));

        int max = sub.getUsages().size();
        int i = 0;
        for (CommandUsage<C> commandUsage : sub.getUsages()) {
            var formatInfo = formatUsageTillSub(dispatcher, command, commandUsage);
            source.reply(BRANCH_DOWN.append(BRANCH_FORWARDS)
                    .append(formatter.formatUsageOnly(formatInfo.right())));
            if (commandUsage.hasParamType(Command.class)) {
                displayUsage(dispatcher, command, source, commandUsage,
                        formatInfo.left(), formatter, formatInfo.right(), i == max - 1);
            }
            i++;
        }

    }

}
