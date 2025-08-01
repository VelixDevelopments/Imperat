package dev.velix.imperat.commands;

import dev.velix.imperat.annotations.*;
import dev.velix.imperat.components.TestSource;

@Command("testac")
public class TestAC {

    @Usage
    public void onUsage(
            TestSource source,
            @Suggest("any_text") @Named("text") String text,
            @Default("1") @Suggest({"2", "5", "10"}) @Named("count") Integer count
    ) {
        // /testac <text> [count]
    }
}