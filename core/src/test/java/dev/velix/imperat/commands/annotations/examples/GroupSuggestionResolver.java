package dev.velix.imperat.commands.annotations.examples;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.resolvers.TypeSuggestionResolver;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GroupSuggestionResolver implements TypeSuggestionResolver<TestSource, Group> {

    @Override
    public @NotNull TypeWrap<Group> getType() {
        return TypeWrap.of(Group.class);
    }

    @Override
    public List<String> autoComplete(
            SuggestionContext<TestSource> context,
            CommandParameter<TestSource> parameter
    ) {
        return GroupRegistry.getInstance().getAll()
                .stream().map(Group::name)
                .toList();
    }

}
