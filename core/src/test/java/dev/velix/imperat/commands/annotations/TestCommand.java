package dev.velix.imperat.commands.annotations;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.annotations.types.*;

@Command("test")
@Inherit(FirstSub.class)
public class TestCommand {

    //TODO test command methods
    @Usage
    public void defaultExec(TestSource source) {
        source.reply("Default execution of test(root) command");
    }

    @Usage
    public void cmdUsage(TestSource source, @Named("otherText") @Suggest({"hi", "bye"}) String otherText) {
        source.reply("Executing usage in test's main usage, num= " + otherText);
    }

    @SubCommand("othersub")
    public void doOtherSub(TestSource source, @Named("text") @Suggest({"hi", "bye"}) String text) {
        source.reply("Other-value= " + text);
    }
    
    @Command("embedded")
    public void embeddedCmd(TestSource source, @Named("value") String arg) {
        source.reply("Embedded command value=" + arg);
    }
    
}
