package dev.velix.imperat.paramtypes;

import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Greedy;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.Usage;
import dev.velix.imperat.components.TestSource;

import java.util.Optional;

@Command("testoptional")
public class TestJavaOptionalParam {

    @Usage
    public void exec(TestSource source, @Named("text") @Greedy Optional<String> textContainer) {
        textContainer.ifPresent((txt)-> source.reply("Text entered= '" + txt +"'"));
    }

}
