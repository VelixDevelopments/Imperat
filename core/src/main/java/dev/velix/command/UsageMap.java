package dev.velix.command;

import dev.velix.command.parameters.CommandParameter;
import dev.velix.context.Source;
import org.jetbrains.annotations.NotNull;

import java.util.*;

final class UsageMap<S extends Source> extends HashMap<List<CommandParameter>, CommandUsage<S>> implements Iterable<CommandUsage<S>> {
    
    private final LinkedHashSet<CommandUsage<S>> sort = new LinkedHashSet<>();
    
    UsageMap() {
        super();
    }
    
    @Override
    public CommandUsage<S> put(List<CommandParameter> key, CommandUsage<S> value) {
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
