package dev.velix.imperat.caption.premade;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.caption.CaptionKey;
import dev.velix.imperat.caption.Messages;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ResolvedContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NoHelpCaption<C> implements Caption<C> {
    /**
     * @return the key
     */
    @Override
    public @NotNull CaptionKey getKey() {
        return CaptionKey.NO_HELP_AVAILABLE_CAPTION;
    }
    
    /**
     * @param dispatcher the command dispatcher
     * @param context    the context
     * @param exception  the exception may be null if no exception provided
     * @return The message in the form of a component
     */
    @Override
    public @NotNull String getMessage(@NotNull Imperat<C> dispatcher,
                                      @NotNull Context<C> context,
                                      @Nullable Exception exception) {
        Command<C> cmdUsed;
        if (context instanceof ResolvedContext<C> resolvedContext) {
            cmdUsed = resolvedContext.getLastUsedCommand();
        } else {
            cmdUsed = dispatcher.getCommand(context.getCommandUsed());
        }
        assert cmdUsed != null;
        return Messages.NO_HELP_AVAILABLE.replace("<command>", cmdUsed.getName());
    }
}