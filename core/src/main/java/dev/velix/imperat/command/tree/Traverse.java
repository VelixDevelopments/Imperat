package dev.velix.imperat.command.tree;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.CommandDebugger;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Traverse implements Iterable<CommandParameter> {

    private final List<CommandParameter> parameters = new ArrayList<>();

    @Setter
    private TraverseResult result;

    private Traverse(TraverseResult result) {
        this.result = result;
    }

    public static Traverse of(TraverseResult result) {
        return new Traverse(result);
    }

    public static Traverse of() {
        return of(TraverseResult.UNKNOWN);
    }

    public void append(UsageNode<?> node) {
        if (node == null) return;
        if (parameters.contains(node.data)) return;
        parameters.add(node.data);
    }

    public CommandParameter getLastParameter() {
        return parameters.get(parameters.size() - 1);
    }

    @Override
    public @NotNull Iterator<CommandParameter> iterator() {
        return parameters.iterator();
    }

    public TraverseResult result() {
        return result;
    }

    public <S extends Source> @Nullable CommandUsage<S> toUsage(Command<S> command) {
        return command.getUsage(parameters);
    }

    public void visualize() {
        CommandDebugger.debug("Result => " + result.name());
        StringBuilder builder = new StringBuilder();
        for (var node : parameters) {
            builder.append(node.format()).append(" -> ");
        }
        CommandDebugger.debug(builder.toString());
    }


}
