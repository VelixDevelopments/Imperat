package dev.velix.imperat.commands.annotations.examples;

import dev.velix.imperat.annotations.*;
import dev.velix.imperat.components.TestSource;
import org.jetbrains.annotations.Nullable;

@Command("ban2")
@Permission("command.ban")
@Description("Main command for banning players")
public final class Ban2Command {
    
    @Usage
    public void showUsage(TestSource source) {
        source.reply("/ban <player> [-silent] [duration] [reason...]");
    }
    
    @Usage
    public void ban(
            TestSource source,
            @Named("target") String player,
            @Flag({"time", "t"}) @Nullable String time,
            @Named("reason") @Default("Breaking server laws") @Optional @Greedy String reason
    ) {
        //TODO actual ban logic
        String durationFormat = time == null ? "FOREVER" : "for " + time;
        String msg = "Banning " + player + " " + durationFormat + " due to '" + reason + "'";
        System.out.println(msg);
    }
}