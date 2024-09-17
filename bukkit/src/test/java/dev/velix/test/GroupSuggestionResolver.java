package dev.velix.test;

import dev.velix.BukkitSource;
import dev.velix.command.parameters.CommandParameter;
import dev.velix.context.SuggestionContext;
import dev.velix.resolvers.BukkitSuggestionResolver;
import dev.velix.util.TypeWrap;

import java.util.List;
import java.util.stream.Collectors;

public class GroupSuggestionResolver implements BukkitSuggestionResolver<Group> {
    
    @Override
    public TypeWrap<Group> getType() {
        return TypeWrap.of(Group.class);
    }
    
    @Override
    public List<String> autoComplete(SuggestionContext<BukkitSource> context, CommandParameter parameterToComplete) {
        return GroupRegistry.getInstance().getAll()
                .stream().map(Group::name)
                .collect(Collectors.toList());
    }
    
    
}
