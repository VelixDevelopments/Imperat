package dev.velix.imperat.context.internal;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
class DefaultContextFactory<S extends Source> implements ContextFactory<S> {


    DefaultContextFactory() {
    }

    /**
     * @param source  the sender/source of this command execution
     * @param command the command label used
     * @param queue   the args input
     * @return new context from the command and args used by {@link Source}
     */
    @Override
    public @NotNull Context<S> createContext(
            @NotNull Imperat<S> dispatcher,
            @NotNull S source,
            @NotNull Command<S> command,
            @NotNull ArgumentQueue queue
    ) {
        return new ContextImpl<>(dispatcher, command, source, queue);
    }

    /**
     * @param command      the command that's running
     * @param plainContext the context plain
     * @return the context after resolving args into values for
     * later on parsing it into the execution
     */
    @Override
    public ResolvedContext<S> createResolvedContext(
            @NotNull Imperat<S> dispatcher,
            @NotNull Command<S> command,
            @NotNull Context<S> plainContext,
            @NotNull CommandUsage<S> usage
    ) {
        return new ResolvedContextImpl<>(
                dispatcher,
                command,
                plainContext,
                usage
        );
    }
}
