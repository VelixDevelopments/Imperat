package dev.velix.imperat.help.templates;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.help.PaginatedHelpTemplate;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class DefaultPaginatedTemplate extends DefaultTemplate implements PaginatedHelpTemplate {
    
    private final int syntaxesPerPage;
    
    public DefaultPaginatedTemplate(
            int syntaxesPerPage) {
        super();
        this.syntaxesPerPage = syntaxesPerPage;
    }
    
    
    @Override
    public int syntaxesPerPage() {
        return syntaxesPerPage;
    }
    
    @Override
    public String fullHeader(Command<?> command, int page, int maxPages) {
        return "<dark_gray><bold><strikethrough>=================== <green>"
                + command.getName() + "'s help " + pagesHeaderComponent(page, maxPages) +
                " </green>===================";
        
    }
    
    
    private String pagesHeaderComponent(int currentPage, int maxPage) {
        return "<gold>(<yellow>" + currentPage
                + "<gray>/</gray>" + maxPage + "</yellow>)";
    }
    
}
