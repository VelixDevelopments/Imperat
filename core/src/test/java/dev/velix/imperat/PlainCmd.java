package dev.velix.imperat;

import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.Suggest;
import dev.velix.imperat.annotations.Usage;
import dev.velix.imperat.components.TestSource;

@Command("plain")
public final class PlainCmd {
    
    @Usage
    public void t(
            TestSource source,
            @Named("txt") @Suggest({"hi", "hello", "bruh", "awesomeee", "i-love-imperat"}) String txt) {
        
    }
    
}
