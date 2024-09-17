package dev.velix.imperat.examples.help;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.help.PaginatedHelpTemplate;

public final class ExamplePaginatedHelpTemplate
        extends ExampleHelpTemplate implements PaginatedHelpTemplate {
    
    @Override
    public int syntaxesPerPage() {
        return 5;
    }
    
    @Override
    public String fullHeader(Command<?> command, int page, int maxPages) {
        return "&8&l&m===================&r &2"
                + command.name() + "'s help " + pagesHeaderComponent(page, maxPages) + "&r &8&l&m===================";
    }
    
    
    private String pagesHeaderComponent(int page, int maxPages) {
        return "&8( &a" + page + "&7/" + maxPages + " &8)";
    }
    
}
