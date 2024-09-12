package dev.velix.imperat.commands.annotations.examples;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.suggestions.CompletionArg;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class GroupSuggestionResolver implements SuggestionResolver<TestSource, Group> {
    
    @Override
    public TypeWrap<Group> getType() {
        return TypeWrap.of(Group.class);
    }
    
    @Override
    public List<String> autoComplete(Command<TestSource> command, TestSource source,
                                     ArgumentQueue queue, CommandParameter parameterToComplete, @Nullable CompletionArg argToComplete) {
        return GroupRegistry.getInstance().getAll()
                .stream().map(Group::name)
                .collect(Collectors.toList());
    }
    
}
