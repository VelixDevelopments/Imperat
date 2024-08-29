package dev.velix.imperat.examples.help;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.caption.Messages;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.help.UsageFormatter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ExampleUsageFormatter implements UsageFormatter {


    @Override
    public <C> Component formatUsageLine(@NotNull Imperat<C> dispatcher, Command<C> command, CommandUsage<C> usage, boolean isLast) {
        String format = dispatcher.commandPrefix() + CommandUsage.format(command, usage);
        String msg = "<green>" + format + " <white><bold>-</bold></white> <yellow>" + usage.getDescription();
        return Messages.getMsg(msg);
    }

    @Override
    public Component formatUsageOnly(String formattedUsage) {
        String msg = "<green>" + formattedUsage;
        return Messages.getMsg(msg);
    }
}
