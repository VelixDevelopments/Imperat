package dev.velix.imperat.context.internal;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.suggestions.CompletionArg;
import dev.velix.imperat.context.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a way for defining
 * how the context is created
 *
 * @param <S>
 */
@ApiStatus.AvailableSince("1.0.0")
public abstract class ContextFactory<S extends Source> {
    
    protected final Imperat<S> imperat;
    
    protected ContextFactory(Imperat<S> imperat) {
        this.imperat = imperat;
    }
    
    
    public static <S extends Source> ContextFactory<S> defaultFactory(Imperat<S> imperat) {
        return new DefaultContextFactory<>(imperat);
    }


    /**
     * @param source     the sender/source of this command execution
     * @param command    the command label used
     * @param queue      the args input
     * @return new context from the command and args used by {@link Source}
     */
    @NotNull
    public abstract Context<S> createContext(
            @NotNull S source,
            @NotNull Command<S> command,
            @NotNull ArgumentQueue queue
    );

    /**
     * @param source     the source
     * @param command    the command
     * @param queue      the argument input
     * @param arg        the arg being completed
     * @return new context for auto completions with {@link CompletionArg}
     */
    public abstract SuggestionContext<S> createSuggestionContext(
            @NotNull S source,
            @NotNull Command<S> command,
            @NotNull ArgumentQueue queue,
            @NotNull CompletionArg arg
    );

    /**
     * @param plainContext the context plain
     * @param usage        the command usage
     * @return the context after resolving args into values for
     * later on parsing it into the execution
     */
    public abstract ResolvedContext<S> createResolvedContext(
            @NotNull Context<S> plainContext,
            @NotNull CommandUsage<S> usage
    );

}
