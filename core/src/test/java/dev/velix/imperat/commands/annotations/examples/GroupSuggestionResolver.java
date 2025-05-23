package dev.velix.imperat.commands.annotations.examples;

import dev.velix.imperat.components.TestSource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.resolvers.SuggestionResolver;

import java.util.List;

public class GroupSuggestionResolver implements SuggestionResolver<TestSource> {


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
