package dev.velix.commands.annotations.examples;

import dev.velix.TestSource;
import dev.velix.command.parameters.CommandParameter;
import dev.velix.context.ExecutionContext;
import dev.velix.context.internal.sur.Cursor;
import dev.velix.exception.ImperatException;
import dev.velix.exception.SourceException;
import dev.velix.resolvers.ValueResolver;

public final class GroupValueResolver implements ValueResolver<TestSource, Group> {
    
    @Override
    public Group resolve(
            ExecutionContext<TestSource> context,
            CommandParameter parameter,
            Cursor cursor,
            String raw
    ) throws ImperatException {
        /*if (sender.isConsole()) {
            throw new SenderErrorException("Invalid group '%s'", raw);
        }*/
        var group = GroupRegistry.getInstance()
                .getData(raw);
        if (group.isEmpty()) {
            throw new SourceException("Invalid group '%s'", raw);
        }
        return group.get();
    }
}
