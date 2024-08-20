package dev.velix.imperat.help.templates;

import dev.velix.imperat.caption.Messages;
import dev.velix.imperat.help.PaginatedHelpTemplate;
import net.kyori.adventure.text.Component;
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
    public Component pagesHeaderComponent(int currentPage, int maxPage) {
        return Messages.getMsg("<gold>(<yellow>" +
                currentPage + "<gray>/</gray>" + maxPage + "</yellow>)");
    }

}
