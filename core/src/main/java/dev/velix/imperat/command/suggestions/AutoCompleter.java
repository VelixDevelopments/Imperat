package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import org.jetbrains.annotations.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a class that's responsible for
 * Handling all auto-completion processes during
 * tab-completion per one command, regardless of the sender valueType
 */
@ApiStatus.AvailableSince("1.0.0")
public abstract class AutoCompleter<S extends Source> {

    protected final Command<S> command;

    protected AutoCompleter(Command<S> command) {
        this.command = command;
    }

    public static <S extends Source> AutoCompleter<S> createNative(Command<S> command) {
        return new NativeAutoCompleter<>(command);
    }

    private static @NotNull CompletionArg getLastArg(String[] args) {
        if (args.length == 0) return new CompletionArg("", -1);
        int index = args.length - 1;
        String result = args[args.length - 1];
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
    public final CompletableFuture<List<String>> autoComplete(
        final Imperat<S> dispatcher,
        final S sender,
        final String[] args
    ) {
        CompletionArg argToComplete = getLastArg(args);
        ArgumentQueue queue = ArgumentQueue.parseAutoCompletion(args, argToComplete.isEmpty());

        SuggestionContext<S> context = dispatcher.config().getContextFactory()
            .createSuggestionContext(dispatcher, sender, command, queue, argToComplete);
        //ImperatDebugger.debug("auto completing !");
        return autoComplete(dispatcher, context)
            .exceptionally((ex) -> {
                dispatcher.config().handleExecutionThrowable(ex, context, AutoCompleter.class, "autoComplete(dispatcher, sender, args)");
                return Collections.emptyList();
            });
    }

    /**
     * Autocompletes an argument from the whole position of the
     * argument-raw input
     *
     * @param imperat the command dispatcher
     * @param context the context for suggestions
     * @return the auto-completed results
     */
    public abstract CompletableFuture<List<String>> autoComplete(
        Imperat<S> imperat,
        SuggestionContext<S> context
    );
}
