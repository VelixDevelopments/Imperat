package dev.velix.imperat.command.tree;

import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class ArgumentNode<S extends Source> extends ParameterNode<S, CommandParameter<S>> {

    private final int priority;
    ArgumentNode(@NotNull CommandParameter<S> data, int depth, @Nullable CommandUsage<S> usage) {
        super(data, depth, usage);
        priority = data.isOptional() ? 2 : 1;
    }

    @Override
    public boolean matchesInput(String input) {
        var type = data.type();
        return type.matchesInput(input, data);
    }

    @Override
    public String format() {
        return data.format();
    }

    @Override
    public int priority() {
        return priority;
    }
    
}
