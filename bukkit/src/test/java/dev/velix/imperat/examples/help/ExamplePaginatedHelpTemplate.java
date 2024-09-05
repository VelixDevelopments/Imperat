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
        return "<dark_gray><bold><strikethrough>=================== <dark_green>"
                + command.getName() + "'s help " + pagesHeaderComponent(page, maxPages) + " </dark_green>===================";
    }


    private String pagesHeaderComponent(int page, int maxPages) {
        return "<dark_gray>(</dark_gray> <green>" + page + "<gray>/</gray>" + maxPages + " <dark_gray>)</dark_gray>";
    }

}
