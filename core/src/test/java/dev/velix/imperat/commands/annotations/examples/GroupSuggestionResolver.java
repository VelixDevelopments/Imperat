package dev.velix.imperat.commands.annotations.examples;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.TypeWrap;

import java.util.List;

public class GroupSuggestionResolver implements SuggestionResolver<TestSource, Group> {
    
    @Override
    public TypeWrap<Group> getType() {
        return TypeWrap.of(Group.class);
    }
    
    @Override
    public List<String> autoComplete(
            SuggestionContext<TestSource> context,
            CommandParameter<TestSource> parameterToComplete
    ) {
        return GroupRegistry.getInstance().getAll()
                .stream().map(Group::name)
                .toList();
    }
    
}
