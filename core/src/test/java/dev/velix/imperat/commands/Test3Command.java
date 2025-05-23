package dev.velix.imperat.commands;

import dev.velix.imperat.components.TestSource;
import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Default;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.SubCommand;
import dev.velix.imperat.annotations.Usage;
import dev.velix.imperat.command.AttachmentMode;

@Command("test3")
public class Test3Command {

    @Usage
    public void def(TestSource source, @Named("input") @Default("hello") String input) {
        source.reply("input=" + input);
    }

    @SubCommand(value = "sub", attachment = AttachmentMode.EMPTY)
    public void subDefaultExecution(TestSource source) {
        source.reply("subcommand - default execution !");
    }

    @SubCommand(value = "sub", attachment = AttachmentMode.EMPTY)
    public void subMainExecution(TestSource source, @Named("sub-input") String subInput) {
        source.reply("sub command input= " + subInput);
    }
    /*
    @SubCommand(value = "sub", attachment = AttachmentMode.EMPTY)
    public static class Sub {
        @Usage
        public void def(TestSource source) {
            source.reply("sub command - default execution");
        }
        @Usage
        public void sub(TestSource source, @Named("sub-input") String subInput) {
            source.reply("sub command input= " + subInput);
        }
    }*/
}