package dev.velix.exception;

import dev.velix.Imperat;
import dev.velix.context.Context;
import dev.velix.context.Source;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("1.0.0")
public abstract class SelfHandledException extends ImperatException {
    
    /**
     * Handles the exception
     *
     * @param <S>     the command-source type
     * @param imperat the api
     * @param context the context
     */
    public abstract <S extends Source> void handle(Imperat<S> imperat, Context<S> context);
    
}
