package dev.velix.imperat.command;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class UsageMap<S extends Source> extends HashMap<List<CommandParameter<S>>, CommandUsage<S>> implements Iterable<CommandUsage<S>> {

    private final LinkedHashSet<CommandUsage<S>> sort = new LinkedHashSet<>();

    UsageMap() {
        super();
    }

    @Override
    public CommandUsage<S> put(List<CommandParameter<S>> key, CommandUsage<S> value) {
        sort.add(value);
        return super.put(key, value);
    }

    public Set<CommandUsage<S>> asSortedSet() {
        return sort;
    }

    @Override
    public @NotNull Iterator<CommandUsage<S>> iterator() {
        return sort.iterator();
    }

}
