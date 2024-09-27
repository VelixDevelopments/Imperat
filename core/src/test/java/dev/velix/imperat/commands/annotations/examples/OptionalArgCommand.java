package dev.velix.imperat.commands.annotations.examples;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Default;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.Usage;
import org.jetbrains.annotations.NotNull;

@Command("opt")
public class OptionalArgCommand {
    
    @Usage
    public void mainUsage(TestSource source, @Named("a") String arg1, @Named("b") @Default("hello-world") @NotNull String b) {
        source.reply("A=" + arg1 + ", B= " + b);
    }
    
}
