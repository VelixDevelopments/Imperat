package dev.velix.processors;

import dev.velix.Imperat;
import dev.velix.TestRun;
import dev.velix.TestSource;
import dev.velix.command.processors.CommandPostProcessor;
import dev.velix.context.ResolvedContext;
import dev.velix.exception.ImperatException;

public class CustomPostProcessor implements CommandPostProcessor<TestSource> {
    @Override
    public void process(
            Imperat<TestSource> imperat,
            ResolvedContext<TestSource> context
    ) throws ImperatException {
        TestRun.POST_PROCESSOR_INT++;
    }
}
