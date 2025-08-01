package dev.velix.imperat.commands;

import dev.velix.imperat.annotations.SubCommand;
import dev.velix.imperat.annotations.Usage;
import dev.velix.imperat.components.TestSource;

@MyCustomAnnotation(name = "testreplacer")
public class TestCustomAnnotationCmd {

    @Usage
    public void def(TestSource source) {
        source.reply("DEF");
    }
    @SubCommand("teto")
    interface Teto {

    }
}
