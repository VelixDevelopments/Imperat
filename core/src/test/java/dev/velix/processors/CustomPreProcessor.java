package dev.velix.processors;

import dev.velix.Imperat;
import dev.velix.TestRun;
import dev.velix.TestSource;
import dev.velix.command.CommandUsage;
import dev.velix.command.processors.CommandPreProcessor;
import dev.velix.context.Context;
import dev.velix.exception.ImperatException;

public final class CustomPreProcessor implements CommandPreProcessor<TestSource> {
    @Override
    public void process(
            Imperat<TestSource> imperat,
            Context<TestSource> context,
            CommandUsage<TestSource> usage
    ) throws ImperatException {
        TestRun.PRE_PROCESSOR_INT++;
    }
}
