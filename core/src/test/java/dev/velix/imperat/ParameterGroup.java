package dev.velix.imperat;

import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.commands.annotations.examples.Group;
import dev.velix.imperat.commands.annotations.examples.GroupRegistry;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class ParameterGroup extends BaseParameterType<TestSource, Group> {
    ParameterGroup() {
        super(TypeWrap.of(Group.class));
    }

    @Override
    public @Nullable Group resolve(
        ExecutionContext<TestSource> context,
        @NotNull CommandInputStream<TestSource> commandInputStream
    ) throws ImperatException {
        String raw = commandInputStream.currentRaw().orElse(null);
        if (raw == null) {
            return null;
        }
        return GroupRegistry.getInstance().getData(raw)
            .orElseThrow(() -> new SourceException("Unknown group '%s'", raw));
    }


    @Override
    public Collection<String> suggestions() {
        return GroupRegistry.getInstance().getAll().stream()
            .map(Group::name).toList();
    }
}
