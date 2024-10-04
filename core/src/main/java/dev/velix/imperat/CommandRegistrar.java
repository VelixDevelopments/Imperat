package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface CommandRegistrar<S extends Source> permits Imperat {
	
	/**
	 * Registering a command into the dispatcher
	 *
	 * @param command the command to register
	 */
	void registerCommand(Command<S> command);
	
	/**
	 * Registers a command class built by the
	 * annotations using a parser
	 *
	 * @param command the annotated command instance to parse
	 */
	void registerCommand(Object command);
	
	/**
	 * @param name the name/alias of the command
	 * @return fetches {@link Command} with specific name
	 */
	@Nullable
	Command<S> getCommand(final String name);
	
	/**
	 * @param parameter the parameter
	 * @return the command from the parameter's name
	 */
	default @Nullable Command<S> getCommand(final CommandParameter<S> parameter) {
		return getCommand(parameter.name());
	}
	
	/**
	 * @param owningCommand the command owning this sub-command
	 * @param name          the name of the subcommand you're looking for
	 * @return the subcommand of a command
	 */
	@Nullable
	Command<S> getSubCommand(final String owningCommand, final String name);
	
	/**
	 * Gets all registered commands
	 *
	 * @return the registered commands
	 */
	Collection<? extends Command<S>> getRegisteredCommands();
	
}
