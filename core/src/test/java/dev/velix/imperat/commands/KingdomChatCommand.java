package dev.velix.imperat.commands;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Greedy;
import dev.velix.imperat.annotations.Usage;

@Command("kingdomchat")
public class KingdomChatCommand {

    @Usage
    public void def(TestSource source) {
        source.reply("This is the default usage of the kingdomchat command.");
    }

    @Usage
    public void mainUsage(TestSource source, @Greedy String message) {
        source.reply("Your message: " + message);
    }
}
