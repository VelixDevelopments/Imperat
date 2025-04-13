package dev.velix.imperat.help;

import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;

import java.util.Collection;

@FunctionalInterface
public interface UsageDisplayer<S extends Source> {

    void accept(ExecutionContext<S> ctx, S source, Collection<? extends CommandUsage<S>> commandUsages);

}
