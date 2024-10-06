package dev.velix.imperat.exception;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("1.0.0")
public abstract class SelfHandledException extends ImperatException {

    /**
     * Handles the exception
     *
     * @param <S>     the command-source valueType
     * @param imperat the api
     * @param context the context
     */
    public abstract <S extends Source> void handle(Imperat<S> imperat, Context<S> context);

}
