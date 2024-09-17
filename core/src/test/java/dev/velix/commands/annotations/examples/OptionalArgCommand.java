package dev.velix.commands.annotations.examples;

import dev.velix.TestSource;
import dev.velix.annotations.Command;
import dev.velix.annotations.Default;
import dev.velix.annotations.Named;
import dev.velix.annotations.Usage;
import org.jetbrains.annotations.NotNull;

@Command("opt")
public class OptionalArgCommand {
    
    @Usage
    public void mainUsage(TestSource source, @Named("a") String arg1, @Named("b") @Default("hello-world") @NotNull String b) {
        source.reply("A=" + arg1 + ", B= " + b);
    }
    
}
