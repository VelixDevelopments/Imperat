package dev.velix.imperat.context;

import dev.velix.imperat.CommandSource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a flag that has been parsed/read/loaded from a
 * context of a command entered by the {@link CommandSource}
 */
@ApiStatus.AvailableSince("1.0.0")
public interface CommandFlag {

	/**
	 * The main name of the flag
	 *
	 * @return the name(unique) of the flag
	 */
	@NotNull
	String name();

	/**
	 * @return the alias of the flag
	 */
	@NotNull
	String alias();

	default boolean hasAlias(String alias) {
		return alias().equalsIgnoreCase(alias);
	}

	static CommandFlag create(String name, String alias) {
		return new CommandFlagImpl(name, alias);
	}


	record CommandFlagImpl(String name, String alias)
			  implements CommandFlag {
	}

}
