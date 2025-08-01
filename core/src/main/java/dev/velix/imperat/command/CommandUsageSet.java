package dev.velix.imperat.command;

import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

final class CommandUsageSet<S extends Source> implements Iterable<CommandUsage<S>> {

    private final LinkedHashSet<CommandUsage<S>> sort = new LinkedHashSet<>();

    CommandUsageSet() {
        super();
    }

    public CommandUsageSet<S> put(CommandUsage<S> value) {
        sort.add(value);
        return this;
    }

    public Set<CommandUsage<S>> asSortedSet() {
        return sort;
    }

    @Override
    public @NotNull Iterator<CommandUsage<S>> iterator() {
        return sort.iterator();
    }

}
