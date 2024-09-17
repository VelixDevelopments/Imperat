package dev.velix.commands.annotations;

import dev.velix.TestSource;
import dev.velix.annotations.Named;
import dev.velix.annotations.SubCommand;
import dev.velix.annotations.Usage;

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
