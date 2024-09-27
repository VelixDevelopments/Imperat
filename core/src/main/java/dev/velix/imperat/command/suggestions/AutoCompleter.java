package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents a class that's responsible for
 * Handling all auto-completion processes during
 * tab-completion per one command, regardless of the sender type
 */
@ApiStatus.AvailableSince("1.0.0")
public final class AutoCompleter<S extends Source> {
    
    private final Command<S> command;
    
    private AutoCompleter(Command<S> command) {
        this.command = command;
    }
    
    public static <S extends Source> AutoCompleter<S> createNative(Command<S> command) {
        return new AutoCompleter<>(command);
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
    public Collection<String> autoComplete(
            final Imperat<S> dispatcher,
            final S sender,
            final String[] args
    ) {
        CompletionArg argToComplete = getLastArg(args);
        ArgumentQueue queue = ArgumentQueue.parseAutoCompletion(args);
        
        SuggestionContext<S> context = dispatcher.getContextFactory()
                .createSuggestionContext(dispatcher, sender, command, queue, argToComplete);
        
        return autoComplete(dispatcher, context);
    }
    
    /**
     * Autocompletes an argument from the whole position of the
     * argument-raw input
     *
     * @param dispatcher the command dispatcher
     * @param context    the context for suggestions
     * @return the auto-completed results
     */
    public Collection<String> autoComplete(
            Imperat<S> dispatcher,
            SuggestionContext<S> context
    ) {
        return command.tabComplete(dispatcher, context);
    }
}
