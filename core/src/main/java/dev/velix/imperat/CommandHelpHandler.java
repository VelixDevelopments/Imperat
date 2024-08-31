package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.help.HelpTemplate;
import org.jetbrains.annotations.NotNull;

public sealed interface CommandHelpHandler<C> permits Imperat {
	
	
	/**
	 * @return The template for showing help
	 */
	@NotNull
	HelpTemplate getHelpTemplate();
	
	/**
	 * Set the help template to use
	 *
	 * @param template the help template
	 */
	void setHelpTemplate(HelpTemplate template);
	
	/**
	 * Creates an instance of {@link CommandHelp}
	 *
	 * @param command       the command
	 * @param context       the context
	 * @return {@link CommandHelp} for the command usage used in a certain context
	 */
	CommandHelp<C> createCommandHelp(Command<C> command, Context<C> context);
	
}
