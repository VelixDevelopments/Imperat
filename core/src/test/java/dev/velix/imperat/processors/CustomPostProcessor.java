package dev.velix.imperat.processors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.TestRun;
import dev.velix.imperat.components.TestSource;
import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.exception.ImperatException;

public class CustomPostProcessor implements CommandPostProcessor<TestSource> {
    @Override
    public void process(
            Imperat<TestSource> imperat,
            ExecutionContext<TestSource> context
    ) throws ImperatException {
        TestRun.POST_PROCESSOR_INT++;
    }
}
