package dev.velix.imperat.processors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.TestRun;
import dev.velix.imperat.TestSource;
import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.exceptions.CommandException;

public class CustomPostProcessor implements CommandPostProcessor<TestSource> {
    @Override
    public void process(
            Imperat<TestSource> imperat,
            ResolvedContext<TestSource> context
    ) throws CommandException {
        TestRun.POST_PROCESSOR_INT++;
    }
}
