package dev.velix.imperat.commands;

import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Default;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.Suggest;
import dev.velix.imperat.annotations.Usage;
import dev.velix.imperat.components.TestSource;

@Command("testac2")
public class TestAC2 {

    @Usage
    public void onUsage(
            TestSource source,
            @Suggest("any_text") @Named("text") String text,
            @Default("1") @Suggest({"2", "5", "10"}) @Named("count") Integer count,
            @Default("2") @Suggest({"3", "6", "9"}) @Named("extra") Integer extra
    ) {
        // /testac <text> [count] [extra]
    }
}