package dev.velix.imperat.exception;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;

public final class NoHelpPageException extends SelfHandledException {

    @Override
    public <S extends Source> void handle(Imperat<S> imperat, Context<S> context) {
        if (!(context instanceof ResolvedContext<S> resolvedContext) || resolvedContext.getDetectedUsage() == null
                || resolvedContext.getDetectedUsage().isHelp()) {
            throw new IllegalCallerException("Called NoHelpPageCaption in wrong the wrong sequence/part of the code");
        }
        
        int page = context.getArgumentOr("page", 1);
        context.getSource().error("Page '<page>' doesn't exist!".replace("<page>", String.valueOf(page)));
    }

}
