package dev.velix.imperat;

import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.Usage;

@Command("test4")
public class Test4Cmd {

    @Usage
    public void exec(TestSource source, @Named("enumHere") CustomEnum customEnum) {
        source.reply("Custom enum input: " + customEnum.name());
    }

}
