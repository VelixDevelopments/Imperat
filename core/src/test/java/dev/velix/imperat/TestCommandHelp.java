package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.help.CommandHelp;

public final class TestCommandHelp extends CommandHelp<TestSource> {
    public TestCommandHelp(
            Imperat<TestSource> dispatcher,
            Command<TestSource> command,
            Context<TestSource> context
    ) {
        super(dispatcher, command, context);
    }
}
