package dev.velix.examples;

import dev.velix.BukkitSource;
import dev.velix.annotations.Command;
import dev.velix.annotations.Named;
import dev.velix.annotations.Range;
import dev.velix.annotations.Usage;

@Command("example")
public final class ExampleCommand {
    
    @Usage
    public void defaultUsage(BukkitSource source) {
        source.reply("This is just an example with no arguments entered");
    }
    
    @Usage
    public void exampleOneArg(BukkitSource source, @Named("firstArg") @Range(min = 5, max = 10) int firstArg) {
        source.reply("Entered required num= " + firstArg);
    }
    
}
