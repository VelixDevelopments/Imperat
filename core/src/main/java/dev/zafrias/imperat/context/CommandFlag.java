package dev.zafrias.imperat.context.flags;

import dev.zafrias.imperat.CommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a flag that has been parsed/read/loaded from a
 * context of a command entered by the {@link CommandSource}
 */
public interface CommandFlag {

	/**
	 * The main name of the flag
	 * @return the name(unique) of the flag
	 */
	@NotNull String getName();

	/**
	 * @return the aliases of the flag
	 */
	@NotNull
	List<String> getAliases();

	default boolean hasAlias(String alias) {
		return getAliases().contains(alias.toLowerCase());
	}

}
