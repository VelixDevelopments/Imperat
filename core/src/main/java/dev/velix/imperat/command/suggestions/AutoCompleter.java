package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Represents a class that's responsible for
 * Handling all auto-completion processes during
 * tab-completion per one command, regardless of the sender type
 */
@ApiStatus.AvailableSince("1.0.0")
public abstract class AutoCompleter<S extends Source> {
    
    protected final Command<S> command;
    
    protected AutoCompleter(Command<S> command) {
        this.command = command;
    }
    
    public static <S extends Source> AutoCompleter<S> simple(Command<S> command) {
        return new SimpleAutoCompleter<>(command);
    }
    
    public static <S extends Source> AutoCompleter<S> advanced(Command<S> command) {
        return new AdvancedAutoCompleter<>(command);
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
    public abstract List<String> autoComplete(Imperat<S> dispatcher,
                              S sender, String[] args);

    /**
     * Autocompletes an argument from the whole position of the
     * argument-raw input
     *
     * @param dispatcher the command dispatcher
     * @param source     the sender of the auto-completion
     * @param currentArg the arg being completed
     * @param args       the args for raw input
     * @return the auto-completed results
     */
    public abstract List<String> autoCompleteArgument(Imperat<S> dispatcher,
                                      S source, CompletionArg currentArg, String[] args);
}
