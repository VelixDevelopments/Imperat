package dev.velix.imperat.command.returns;

import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;

import java.lang.reflect.Type;

public interface ReturnResolver<S extends Source, T> {

    /**
     * Handles the return value of a command.
     *
     * @param context The execution contex of the command.
     * @param value  The return value of the command.
     */
    void handle(ExecutionContext<S> context, T value);

    /**
     * Returns the type of the return value.
     *
     * @return The type of the return value.
     */
    Type getType();

}
