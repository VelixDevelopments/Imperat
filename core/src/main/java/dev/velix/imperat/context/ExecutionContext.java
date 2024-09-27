package dev.velix.imperat.context;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.internal.ResolvedArgument;
import dev.velix.imperat.context.internal.ResolvedFlag;
import dev.velix.imperat.exception.ImperatException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * Represents the context capabilities
 * during the execution of a command
 */
@ApiStatus.AvailableSince("1.0.0")
public interface ExecutionContext<S extends Source> extends Context<S> {
    
    /**
     * @param flagName the name of the flag to check if it's used or not
     * @return The flag whether it has been used or not in this command context
     */
    ResolvedFlag getFlag(String flagName);
    
    /**
     * Fetches the flag input value
     * returns null if the flag is a {@link CommandSwitch}
     * OR if the value hasn't been resolved somehow
     *
     * @param flagName the flag name
     * @param <T>      the type of the flag value resolved
     * @return the resolved value of the flag input
     */
    @Nullable <T> T getFlagValue(String flagName);
    
    /**
     * Fetches a resolved argument's value
     *
     * @param name the name of the command
     * @param <T>  the type of this value
     * @return the value of the resolved argument
     * @see ResolvedArgument
     */
    <T> @Nullable T getArgument(String name);
    
    default <T> @NotNull T getArgumentOr(String name, T value) {
        final T argValue = getArgument(name);
        if (argValue != null) return argValue;
        return value;
    }
    
    default String getRawArgument(int index) {
        if (index >= arguments().size() || index < 0) return null;
        return arguments().get(index);
    }
    
    /**
     * Resolves source into a new type of source
     * using {@link Imperat#getSourceResolver(Type)}
     *
     * @param <R> the type of the resolved source
     * @return the resolved source {@link R}
     */
    <R> @NotNull R getResolvedSource(Type type) throws ImperatException;
    
    /**
     * Fetches the argument/input resolved by the context
     * using {@link dev.velix.imperat.resolvers.ContextResolver}
     *
     * @param type type of argument to return
     * @param <T>  the type of this argument parsed value
     * @return the argument/input resolved by the context
     */
    <T> @Nullable T getContextResolvedArgument(Class<T> type) throws ImperatException;
    
}
