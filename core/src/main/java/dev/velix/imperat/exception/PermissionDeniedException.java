package dev.velix.imperat.exception;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;

public final class PermissionDeniedException extends SelfHandledException {
    
    @Override
    public <S extends Source> void handle(Imperat<S> imperat, Context<S> context) {
        context.getSource().error("You don't have permission to do that!");
    }
    
}
