package dev.velix.commands.annotations.examples;

import dev.velix.TestSource;
import dev.velix.command.parameters.CommandParameter;
import dev.velix.context.SuggestionContext;
import dev.velix.resolvers.SuggestionResolver;
import dev.velix.util.TypeWrap;

import java.util.List;

public class GroupSuggestionResolver implements SuggestionResolver<TestSource, Group> {
    
    @Override
    public TypeWrap<Group> getType() {
        return TypeWrap.of(Group.class);
    }
    
    @Override
    public List<String> autoComplete(
            SuggestionContext<TestSource> context,
            CommandParameter parameterToComplete
    ) {
        return GroupRegistry.getInstance().getAll()
                .stream().map(Group::name)
                .toList();
    }
    
}
