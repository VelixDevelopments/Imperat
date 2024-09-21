package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.resolvers.PermissionResolver;
import dev.velix.imperat.resolvers.SuggestionResolver;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
@Deprecated
final class SimpleAutoCompleter<S extends Source> extends AutoCompleter<S> {
    
    SimpleAutoCompleter(Command<S> command) {
        super(command);
    }
    
    
    /**
     * Autocompletes an argument from the whole position of the
     * argument-raw input
     *
     * @param dispatcher the command dispatcher
     * @param context the context
     * @return the auto-completed results
     */
    @Override
    public List<String> autoCompleteArgument(Imperat<S> dispatcher, SuggestionContext<S> context) {
        
        var sender = context.source();
        var currentArg = context.getArgToComplete();
        
        final PermissionResolver<S> permResolver = dispatcher.getPermissionResolver();
        if (!command.isIgnoringACPerms() &&
                !permResolver.hasPermission(sender, command.permission())) {
            return Collections.emptyList();
        }
        
        var closestUsages = getClosestUsages(context.arguments());
        int index = currentArg.index();
        if (index == -1)
            index = 0;
        
        AutoCompleteList results = new AutoCompleteList();
        for (CommandUsage<S> usage : closestUsages) {
            if (index < 0 || index >= usage.getMaxLength()) continue;
            CommandParameter<S> parameter = usage.getParameters().get(index);
            if (!command.isIgnoringACPerms() && !permResolver.hasPermission(sender, parameter.permission())) {
                continue;
            }
            if (parameter.isCommand()) {
                results.add(parameter.name());
                parameter.asCommand().aliases().forEach(results::add);
            } else {
                SuggestionResolver<S, ?> resolver = dispatcher.getParameterSuggestionResolver(parameter);
                if (resolver != null) {
                    results.addAll(resolver.autoComplete(context, parameter));
                }
                
            }
            
        }
        
        return results.asList();
    }
    
    
    private Collection<? extends CommandUsage<S>> getClosestUsages(ArgumentQueue queue) {
        
        return command
                .findUsages((usage) -> {
                    if (queue.size() >= usage.getMaxLength()) {
                        for (int i = 0; i < usage.getMaxLength(); i++) {
                            CommandParameter<S> parameter = usage.getParameters().get(i);
                            if (!parameter.isCommand()) continue;
                            
                            if (i >= queue.size()) return false;
                            String corresponding = queue.get(i);
                            if (corresponding != null && !corresponding.isEmpty() &&
                                    !parameter.asCommand().hasName(corresponding))
                                return false;
                        }
                        return true;
                    }
                    return queue.size() <= usage.getMaxLength();
                });
    }
    
    
}
