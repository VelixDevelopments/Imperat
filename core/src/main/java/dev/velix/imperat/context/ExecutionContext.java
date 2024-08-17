package dev.velix.imperat.context;

import dev.velix.imperat.context.internal.ResolvedArgument;
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
	 * @return the arguments entered by the
	 * @see ArgumentQueue
	 */
	@NotNull
	ArgumentQueue getArguments();
	
	
	/**
	 * @param flagName the name of the flag to check if it's used or not
	 * @return The flag whether it has been used or not in this command context
	 */
	boolean getFlag(String flagName);
	
	/**
	 * Fetches a resolved argument's value
	 *
	 * @param name the name of the command
	 * @param <T>  the type of this value
	 * @return the value of the resolved argument
	 * @see ResolvedArgument
	 */
	<T> @Nullable T getArgument(String name);
	
	default String getRawArgument(int index) {
		if (index >= getArguments().size() || index < 0) return null;
		return getArguments().get(index);
	}
	
	
	/**
	 * @return the command label used originally
	 */
	String getCommandUsed();
}
