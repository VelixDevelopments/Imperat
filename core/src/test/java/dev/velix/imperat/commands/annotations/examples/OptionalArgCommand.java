package dev.velix.imperat.commands.annotations.examples;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.annotations.types.Command;
import dev.velix.imperat.annotations.types.DefaultValue;
import dev.velix.imperat.annotations.types.Named;
import dev.velix.imperat.annotations.types.Usage;
import org.jetbrains.annotations.NotNull;

@Command("opt")
public class OptionalArgCommand {
    
    @Usage
    public void mainUsage(TestSource source, @Named("a") String arg1, @Named("b") @DefaultValue("hello-world") @NotNull String b) {
        source.reply("A=" + arg1 + ", B= " + b);
    }
    
}
