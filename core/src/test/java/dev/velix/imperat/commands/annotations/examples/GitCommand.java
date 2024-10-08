package dev.velix.imperat.commands.annotations.examples;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Flag;
import dev.velix.imperat.annotations.SubCommand;
import dev.velix.imperat.annotations.Usage;

@Command("git")
public class GitCommand {

    @Usage
    public void def(TestSource source) {
        source.reply("default usage");
    }

    @SubCommand("commit")
    public void commit(TestSource source, @Flag({"message", "m"}) String msg) {

        // /git commit -m <message>
        System.out.println("Commiting with msg: " + msg);

    }

}
