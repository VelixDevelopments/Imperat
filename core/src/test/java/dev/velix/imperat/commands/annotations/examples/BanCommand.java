package dev.velix.imperat.commands.annotations.examples;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Description;
import dev.velix.imperat.annotations.Greedy;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.Optional;
import dev.velix.imperat.annotations.Permission;
import dev.velix.imperat.annotations.Range;
import dev.velix.imperat.annotations.Switch;
import dev.velix.imperat.annotations.Usage;
import org.jetbrains.annotations.Nullable;

@Command("ban")
@Permission("command.ban")
@Description("Main command for banning players")
public final class BanCommand {

    @Usage
    public void showUsage(TestSource source) {
        source.reply("/ban <player> [-silent] [duration] [reason...]");
    }

    @Usage
    public void ban(
            TestSource source,
            @Named("target") String player,
            @Switch({"silent", "s"}) boolean silent,
            @Switch("ip") boolean ip,
            @Named("duration") @Optional @Nullable String duration,
            @Named("reason") @Optional @Greedy String reason
    ) {
        //TODO actual ban logic
        String durationFormat = duration == null ? "FOREVER" : "for " + duration;
        String msg = "Banning " + player + " " + durationFormat + " due to '" + reason + "'";
        System.out.println("IP= " + ip);
        System.out.println("SILENT= " + silent);


        if (!silent)
            source.reply("NOT SILENT= " + msg);
        else
            source.reply("SILENT= " + msg);
    }

    @Command("printnum")
    public void printNum(TestSource source, @Named("num") @Range(min = 1.0) int num) {
        source.reply("NUM= " + num);
    }

}