package dev.velix.imperat.context.internal;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.CommandSwitch;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.resolvers.ContextResolver;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
final class ContextImpl<S extends Source> implements Context<S> {
    
    private final Imperat<S> dispatcher;
    private final S source;
    private final Command<S> command;
    private final ArgumentQueue args;
    
    ContextImpl(
            Imperat<S> dispatcher,
            Command<S> command,
            S source,
            ArgumentQueue args) {
        this.dispatcher = dispatcher;
        this.source = source;
        this.command = command;
        this.args = args;
    }
    
    
    /**
     * the command used in the context
     *
     * @return the command used
     */
    @Override
    public @NotNull Command<S> getCommandUsed() {
        return command;
    }
    
    /**
     * @return the command source of the command
     * @see Source
     */
    @Override
    public @NotNull S getSource() {
        return source;
    }
    
    /**
     * @return the arguments entered by the
     * @see ArgumentQueue
     */
    @Override
    public @NotNull ArgumentQueue getArguments() {
        return args;
    }
    
    /**
     * @param flagName the name of the flag to check if it's used or not
     * @return The flag whether it has been used or not in this command context
     */
    @Override
    public ResolvedFlag getFlag(String flagName) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Fetches the flag input value
     * returns null if the flag is a {@link CommandSwitch}
     * OR if the value hasn't been resolved somehow
     *
     * @param flagName the flag name
     * @return the resolved value of the flag input
     */
    @Override
    public <T> @Nullable T getFlagValue(String flagName) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Fetches a resolved argument's value
     *
     * @param name the name of the command
     * @return the value of the resolved argument
     * @see ResolvedArgument
     */
    @Override
    public <T> @Nullable T getArgument(String name) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Fetches the argument/input resolved by the context
     * using {@link ContextResolver}
     *
     * @param type type of argument to return
     * @return the argument/input resolved by the context
     */
    @Override
    public <T> @Nullable T getContextResolvedArgument(Class<T> type) {
        throw new UnsupportedOperationException();
    }
    
    
    @Override
    public CommandHelp<S> createCommandHelp() {
        return new CommandHelp<>(dispatcher, command, this);
    }
    
    
    /**
     * @return the number of flags extracted
     */
    @Override
    public int flagsUsedCount() {
        throw new UnsupportedOperationException();
    }
    
}
