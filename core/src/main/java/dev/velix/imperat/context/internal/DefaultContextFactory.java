package dev.velix.imperat.context.internal;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.Source;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
class DefaultContextFactory<C> implements ContextFactory<C> {


    DefaultContextFactory() {
    }

    /**
     * @param source  the sender/source of this command execution
     * @param command the command label used
     * @param queue   the args input
     * @return new context from the command and args used by {@link Source}
     */
    @Override
    public @NotNull Context<C> createContext(
            @NotNull Imperat<C> dispatcher,
            @NotNull Source<C> source,
            @NotNull String command,
            @NotNull ArgumentQueue queue
    ) {
        return new ContextImpl<>(source, command, queue);
    }

    /**
     * @param command      the command that's running
     * @param plainContext the context plain
     * @return the context after resolving args into values for
     * later on parsing it into the execution
     */
    @Override
    public ResolvedContext<C> createResolvedContext(
            @NotNull Imperat<C> dispatcher,
            @NotNull Command<C> command,
            @NotNull Context<C> plainContext,
            @NotNull CommandUsage<C> usage
    ) {
        return new ResolvedContextImpl<>(
                dispatcher,
                command,
                plainContext,
                usage
        );
    }
}
