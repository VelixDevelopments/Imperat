package dev.velix.imperat.processors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.TestRun;
import dev.velix.imperat.TestSource;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.exception.ImperatException;

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
