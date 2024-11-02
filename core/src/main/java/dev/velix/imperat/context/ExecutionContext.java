package dev.velix.imperat.context;

import dev.velix.imperat.ImperatConfig;
import dev.velix.imperat.context.internal.Argument;
import dev.velix.imperat.context.internal.CommandFlag;
import dev.velix.imperat.exception.ImperatException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;

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
    Optional<CommandFlag> getFlag(String flagName);

    /**
     * Fetches the flag input value
     * returns null if the flag is a switch
     * OR if the value hasn't been resolved somehow
     *
     * @param flagName the flag name
     * @param <T>      the valueType of the flag value resolved
     * @return the resolved value of the flag input
     */
    @Nullable
    <T> T getFlagValue(String flagName);

    /**
     * Fetches a resolved argument's value
     *
     * @param name the name of the command
     * @param <T>  the valueType of this value
     * @return the value of the resolved argument
     * @see Argument
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
     * Resolves source into a new valueType of source
     * using {@link ImperatConfig#getSourceResolver(Type)}
     *
     * @param <R> the valueType of the resolved source
     * @return the resolved source {@link R}
     */
    <R> @NotNull R getResolvedSource(Type type) throws ImperatException;

    /**
     * Fetches the argument/input resolved by the context
     * using {@link dev.velix.imperat.resolvers.ContextResolver}
     *
     * @param type valueType of argument to return
     * @param <T>  the valueType of this argument parsed value
     * @return the argument/input resolved by the context
     */
    <T> @Nullable T getContextResolvedArgument(Class<T> type) throws ImperatException;

    /**
     * @return the resolved flag arguments
     */
    Collection<? extends CommandFlag> getResolvedFlags();
}
