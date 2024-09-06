package dev.velix.imperat.commands.annotations;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.annotations.types.Inherit;
import dev.velix.imperat.annotations.types.Named;
import dev.velix.imperat.annotations.types.SubCommand;
import dev.velix.imperat.annotations.types.Usage;

@SubCommand("first")
@Inherit(SecondSub.class)
public final class FirstSub {

    @Usage
    public void defaultUsage(TestSource source) {
        source.reply("Default execution of first sub-command");
    }

    @Usage
    public void cmdUsage(TestSource source, @Named("arg1") String arg1, @Named("arg2") String arg2) {
        source.reply("Executing usage in first's main usage, arg1= " + arg1 + ", arg2= " + arg2);
    }


}
