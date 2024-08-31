package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.Source;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.resolvers.PermissionResolver;
import dev.velix.imperat.resolvers.SuggestionResolver;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
final class AutoCompleterImpl<C> implements AutoCompleter<C> {

    private final Command<C> command;

    AutoCompleterImpl(Command<C> command) {
        this.command = command;
    }

    /**
     * @return The auto-completion command
     */
    @Override
    public Command<C> getCommand() {
        return command;
    }

    private static @NotNull CompletionArg getLastArg(String[] args) {
        if (args.length == 0) return new CompletionArg(null, -1);
        int index = args.length - 1;
        String result = args[args.length - 1];
        if (result.isEmpty() || result.equals(" "))
            result = null;

        return new CompletionArg(result, index);
    }

    /**
     * Autocompletes an argument from the whole position of the
     * argument-raw input
     *
     * @param dispatcher the command dispatcher
     * @param sender     the sender writing the command
     * @param args       the args for raw input
     * @return the auto-completed results
     */
    @Override
    public List<String> autoComplete(Imperat<C> dispatcher,
                                     Source<C> sender, String[] args) {
        CompletionArg argToComplete = getLastArg(args);
        return autoCompleteArgument(dispatcher, sender, argToComplete, args);
    }


    /**
     * Autocompletes an argument from the whole position of the
     * argument-raw input
     *
     * @param dispatcher the command dispatcher
     * @param sender     the sender of the auto-completion
     * @param currentArg the arg being completed
     * @param args       the args for raw input
     * @return the auto-completed results
     */
    @Override
    public List<String> autoCompleteArgument(Imperat<C> dispatcher,
                                             Source<C> sender,
                                             CompletionArg currentArg,
                                             String[] args) {


        final PermissionResolver<C> permResolver = dispatcher.getPermissionResolver();
        if (!command.isIgnoringACPerms() &&
                !permResolver.hasPermission(sender, command.getPermission())) {
            return Collections.emptyList();
        }

        ArgumentQueue queue = ArgumentQueue.parseAutoCompletion(args);
        List<CommandUsage<C>> closestUsages = getClosestUsages(dispatcher, args);
        int index = currentArg.index();
        if (index == -1)
            index = 0;

        AutoCompleteList results = new AutoCompleteList();
        for (CommandUsage<C> usage : closestUsages) {
            if (index < 0 || index >= usage.getMaxLength()) continue;
            CommandParameter parameter = usage.getParameters().get(index);
            if(!command.isIgnoringACPerms() && !permResolver.hasPermission(sender, parameter.getPermission())) {
                continue;
            }
            if (parameter.isCommand()) {
                results.add(parameter.getName());
                parameter.asCommand().getAliases()
                        .forEach(results::add);
            } else {
                SuggestionResolver<C, ?> resolver = dispatcher.getParameterSuggestionResolver(parameter);
                if (resolver != null) {
                    results.addAll(resolver.autoComplete(command, sender.getOrigin(),
                            queue, parameter, currentArg));
                }

            }

        }

        return new ArrayList<>(results.getResults());
    }


    private List<CommandUsage<C>> getClosestUsages(Imperat<C> dispatcher, String[] args) {

        return command.lookup(dispatcher)
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
