package dev.velix.help;

import dev.velix.command.Command;
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
     * The header of the paginated help template
     *
     * @param command  the command
     * @param page     the page
     * @param maxPages the max number of pages
     * @return the paginated header
     */
    String fullHeader(Command<?> command, int page, int maxPages);
}
