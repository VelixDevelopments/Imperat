package dev.zafrias.imperat;

import dev.zafrias.imperat.context.internal.FlagRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

/**
 * Represents a wrapper for the actual command's data
 *
 * @param <C> the command sender type
 */
public interface Command<C> {

	/**
	 * @return the name of the command
	 */
	String getName();

	/**
	 * @return the aliases for this commands
	 */
	@UnmodifiableView
	List<String> getAliases();


	/**
	 * The flags that are  registered
	 * to be usable in this command's syntax
	 *
	 * @return the registered flags for this command
	 */
	@NotNull
	FlagRegistry getKnownFlags();

	//TODO add more stuff here


}
