package dev.velix.imperat.help;

import dev.velix.imperat.command.Command;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a template for holding information that the help-writer will use
 * to display the help menu of a single command to the command sender
 */
@ApiStatus.AvailableSince("1.0.0")
public interface HelpTemplate {
	
	/**
	 * @param command the command
	 * @return the header
	 */
	Component getHeader(Command<?> command);
	
	/**
	 * @param command the command
	 * @return the footer
	 */
	Component getFooter(Command<?> command);
	
	/**
	 * The usage formatter for the help-template
	 *
	 * @return the formatter of the usage
	 */
	UsageFormatter getUsageFormatter();
	
	/**
	 * sets the usage help-formatter
	 *
	 * @param formatter the instance of the formatter to set to
	 */
	void setUsageFormatter(UsageFormatter formatter);
	
	/**
	 * @return the usages displayer
	 */
	UsagesDisplayer getUsagesDisplayer();
	
	/**
	 * Sets the usages displayer instance
	 * to a new one
	 *
	 * @param displayer the displayer instance to set
	 */
	void setUsagesDisplayer(UsagesDisplayer displayer);
	
}
