package dev.velix.imperat.advanced;

import dev.velix.imperat.annotations.Command;

import dev.velix.imperat.annotations.Default;
import dev.velix.imperat.annotations.Flag;
import dev.velix.imperat.annotations.Greedy;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.Usage;
import dev.velix.imperat.components.TestSource;

import java.time.Duration;

@Command("motd")
public class GuildMOTDCommand {

    @Usage
    public void def(TestSource source) {
        source.reply("Default motd execution");
    }

    /*@Usage
    public void mainUsage(TestSource source, @Named("message") String msg, @Named("duration") @Default("24h")Duration duration) {
        source.reply("Message: '" + msg + "'");
        source.reply("Duration: '" + DurationParser.formatDuration(duration) + "'");
    }*/

    @Usage
    public void mainUsage(
            TestSource source,
            @Flag("time") @Default("24h") Duration time,
            @Named("message") @Greedy String message
    ) {
        // /motd [-time <value>] <message...>
        source.reply("Message: '" + message + "'");
        source.reply("Duration: '" + DurationParser.formatDuration(time) + "'");
    }
}
