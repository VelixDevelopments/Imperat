package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
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
     * @param sender     the sender of the auto-completion
     * @param currentArg the value being completed
     * @param args       the args for raw input
     * @return the auto-completed results
     */
    @Override
    public List<String> autoCompleteArgument(Imperat<S> dispatcher,
                                             S sender,
                                             CompletionArg currentArg,
                                             String[] args) {


        final PermissionResolver<S> permResolver = dispatcher.getPermissionResolver();
        if (!command.isIgnoringACPerms() &&
                !permResolver.hasPermission(sender, command.getPermission())) {
            return Collections.emptyList();
        }

        ArgumentQueue queue = ArgumentQueue.parseAutoCompletion(args);
        var closestUsages = getClosestUsages(args);
        int index = currentArg.index();
        if (index == -1)
            index = 0;

        AutoCompleteList results = new AutoCompleteList();
        for (CommandUsage<S> usage : closestUsages) {
            if (index < 0 || index >= usage.getMaxLength()) continue;
            CommandParameter parameter = usage.getParameters().get(index);
            if (!command.isIgnoringACPerms() && !permResolver.hasPermission(sender, parameter.getPermission())) {
                continue;
            }
            if (parameter.isCommand()) {
                results.add(parameter.getName());
                parameter.asCommand().getAliases()
                        .forEach(results::add);
            } else {
                SuggestionResolver<S, ?> resolver = dispatcher.getParameterSuggestionResolver(parameter);
                if (resolver != null) {
                    results.addAll(resolver.autoComplete(command, sender,
                            queue, parameter, currentArg));
                }

            }

        }
        
        return results.asList();
    }


    private Collection<? extends CommandUsage<S>> getClosestUsages(String[] args) {

        return command
                .findUsages((usage) -> {
                    if (args.length >= usage.getMaxLength()) {
                        for (int i = 0; i < usage.getMaxLength(); i++) {
                            CommandParameter parameter = usage.getParameters().get(i);
                            if (!parameter.isCommand()) continue;

                            if (i >= args.length) return false;
                            String corresponding = args[i];
                            if (corresponding != null && !corresponding.isEmpty() &&
                                    !parameter.asCommand().hasName(corresponding))
                                return false;
                        }
                        return true;
                    }
                    return args.length <= usage.getMaxLength();
                });
    }


}
