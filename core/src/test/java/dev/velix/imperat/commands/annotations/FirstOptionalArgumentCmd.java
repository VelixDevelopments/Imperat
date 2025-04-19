package dev.velix.imperat.commands.annotations;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Default;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.Optional;
import dev.velix.imperat.annotations.SubCommand;
import dev.velix.imperat.annotations.Usage;

@Command("foa")
public final class FirstOptionalArgumentCmd {

    @Usage
    public void def(TestSource source, @Named("num") @Optional @Default("1") Integer num) {
        source.reply("Num=" + num);
    }

    @SubCommand("sub")
    public static class MySub {


        @Usage
        public void defaultUsage(TestSource source, @Named("num") Integer num) {
            source.reply("Default execution of sub-command, inherited num='" + num + "'");
        }

        @Usage
        public void mainUsage(TestSource source, @Named("num") Integer num, @Named("num2") Integer num2) {
            source.reply("Main execution of sub-command, inherited num='" + num + "', num2='" + num2 + "'");
        }

    }


}
