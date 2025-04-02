package dev.velix.imperat.commands.annotations;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.SubCommand;
import dev.velix.imperat.annotations.Usage;

@SubCommand("second")
public class SecondSub {

    @Usage
    public void defaultUsage(TestSource source) {
        source.reply("Default execution of second sub-command");
    }

    @Usage
    public void cmdUsage(TestSource source,
                         @Named("otherText") String otherText,
                         @Named("otherText2") String otherText2,
                         @Named("arg1") String arg1,
                         @Named("arg1") String arg2) {
        source.reply("Executing usage in first's main usage," +
            " otherText=" + otherText + ", arg1= " + arg1 + ", arg2= " + arg2);
    }

}
