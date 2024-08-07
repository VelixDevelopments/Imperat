package dev.velix.imperat.help;

import dev.velix.imperat.command.Command;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a help menu with pages
 */
@ApiStatus.AvailableSince("1.0.0")
public interface PaginatedHelpTemplate extends HelpTemplate {

	/**
	 * @return The number of syntaxes to display
	 * per page
	 */
	int syntaxesPerPage();

	/**
	 * @return the pages header component
	 */
	Component pagesHeaderComponent(int page, int maxPages);

	default Component fullHeader(Command<?> command, int page, int maxPages) {
		return getHeader(command).append(pagesHeaderComponent(page, maxPages));
	}
}
