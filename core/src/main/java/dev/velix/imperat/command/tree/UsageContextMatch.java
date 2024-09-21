package dev.velix.imperat.command.tree;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.ImperatDebugger;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class UsageContextMatch<S extends Source> implements Iterable<CommandParameter<S>> {
    
    private final List<CommandParameter<S>> parameters = new ArrayList<>();
    
    @Setter
    private UsageMatchResult result;
    
    private UsageContextMatch(UsageMatchResult result) {
        this.result = result;
    }
    
    public static <S extends Source> UsageContextMatch<S> of(UsageMatchResult result) {
        return new UsageContextMatch<>(result);
    }
    
    public static <S extends Source> UsageContextMatch<S> of() {
        return of(UsageMatchResult.UNKNOWN);
    }
    
    public void append(ParameterNode<S, ?> node) {
        if (node == null) return;
        if (parameters.contains(node.data)) return;
        parameters.add(node.data);
    }
    
    public CommandParameter<S> getLastParameter() {
        return parameters.get(parameters.size() - 1);
    }
    
    @Override
    public @NotNull Iterator<CommandParameter<S>> iterator() {
        return parameters.iterator();
    }
    
    public UsageMatchResult result() {
        return result;
    }
    
    public @Nullable CommandUsage<S> toUsage(Command<S> command) {
        return command.getUsage(parameters);
    }
    
    public void visualize() {
        ImperatDebugger.debug("Result => " + result.name());
        StringBuilder builder = new StringBuilder();
        for (var node : parameters) {
            builder.append(node.format()).append(" -> ");
        }
        ImperatDebugger.debug(builder.toString());
    }
    
}
