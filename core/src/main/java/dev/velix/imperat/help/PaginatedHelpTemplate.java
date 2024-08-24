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
     * The header of the paginated help template
     *
     * @param command the command
     * @param page the page
     * @param maxPages the max number of pages
     *
     * @return the paginated header
     */
   Component fullHeader(Command<?> command, int page, int maxPages);
}
