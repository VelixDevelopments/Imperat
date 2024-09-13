package dev.velix.imperat.examples;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.Range;
import dev.velix.imperat.annotations.Usage;

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
