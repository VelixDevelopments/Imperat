package dev.velix.imperat.exceptions;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.caption.CaptionKey;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;

import static dev.velix.imperat.command.BaseImperat.START_PREFIX;

public final class ExecutionFailure extends CommandException {
    
    private final static String CAPTION_EXECUTION_ERROR_PREFIX = START_PREFIX + "<red><bold>Execution error:</bold></red> ";
    
    private final CaptionKey key;
    
    public ExecutionFailure(CaptionKey key) {
        this.key = key;
    }
    
    /**
     * Handles the exception
     *
     * @param imperat the api
     * @param context the context
     */
    @Override
    public <S extends Source> void handle(Imperat<S> imperat, Context<S> context) {
        Caption<S> caption = imperat.getCaption(key);
        if (caption == null) {
            throw new IllegalStateException(String.format("Unregistered caption from key '%s'", key.id()));
        }
        S source = context.getSource();
        source.reply(CAPTION_EXECUTION_ERROR_PREFIX, caption, context);
    }
    
}
