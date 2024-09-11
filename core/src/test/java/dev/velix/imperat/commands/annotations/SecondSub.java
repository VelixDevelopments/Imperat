package dev.velix.imperat.commands.annotations;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.annotations.types.Named;
import dev.velix.imperat.annotations.types.SubCommand;
import dev.velix.imperat.annotations.types.Usage;

@SubCommand("second")
public class SecondSub {
    
    @Usage
    public void defaultUsage(TestSource source) {
        source.reply("Default execution of second sub-command");
    }
    
    @Usage
    public void cmdUsage(TestSource source,
                         //TODO explain in docs that you don't have to specify any meta-annotations on the arguments that were inherited by their parent
                         @Named("arg1") String arg1,
                         @Named("arg1") String arg2) {
        source.reply("Executing usage in first's main usage," +
                " arg1= " + arg1 + ", arg2= " + arg2);
    }
    
}
