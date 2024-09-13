package dev.velix.imperat.exception;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;

public final class NoHelpException extends SelfHandledException {
    
    @Override
    public <S extends Source> void handle(Imperat<S> imperat, Context<S> context) {
        Command<S> cmdUsed;
        if (context instanceof ResolvedContext<S> resolvedContext) {
            cmdUsed = resolvedContext.getLastUsedCommand();
        } else {
            cmdUsed = imperat.getCommand(context.getCommandUsed());
        }
        assert cmdUsed != null;
        context.getSource().error("No Help available for <yellow>'<command>'".replace("<command>", cmdUsed.getName()));
    }
    
}
