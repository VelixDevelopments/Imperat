package dev.velix.imperat.context;

import dev.velix.imperat.context.internal.ResolvedArgument;
import dev.velix.imperat.context.internal.ResolvedFlag;
import dev.velix.imperat.exceptions.CommandException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the context capabilities
 * during the execution of a command
 */
@ApiStatus.AvailableSince("1.0.0")
public interface ExecutionContext {
    
    /**
     * @return the command label used originally
     */
    String getCommandUsed();
    
    /**
     * @return the arguments entered by the
     * @see ArgumentQueue
     */
    @NotNull
    ArgumentQueue getArguments();


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
        if(argValue != null)return argValue;
        return value;
    }
    
    default String getRawArgument(int index) {
        if (index >= getArguments().size() || index < 0) return null;
        return getArguments().get(index);
    }

    
    /**
     * Fetches the argument/input that is resolved by the context
     * using {@link dev.velix.imperat.resolvers.ContextResolver}
     *
     * @param type type of argument to return
     *
     * @return the argument/input that is resolved by the context
     * @param <T> the type of this argument parsed value
     */
    <T> @Nullable T getContextResolvedArgument(Class<T> type) throws CommandException;
}
