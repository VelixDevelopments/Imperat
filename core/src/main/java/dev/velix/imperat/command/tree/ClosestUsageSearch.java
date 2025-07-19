package dev.velix.imperat.command.tree;

import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Source;

import java.util.Set;

public final class ClosestUsageSearch<S extends Source> {
    
    private final Set<CommandUsage<S>> closestUsages;
    private final CommandUsage<S> closest;
    
    ClosestUsageSearch(Set<CommandUsage<S>> closestUsages) {
        this.closestUsages = closestUsages;
        this.closest = this.closestUsages.iterator().next();
    }
    
    public CommandUsage<S> getClosest() {
        return closest;
    }
    
    public Set<CommandUsage<S>> getClosestUsages() {
        return closestUsages;
    }
}
