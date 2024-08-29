package dev.velix.imperat.examples.help;

import dev.velix.imperat.caption.Messages;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.help.PaginatedHelpTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public final class ExamplePaginatedHelpTemplate
        extends ExampleHelpTemplate implements PaginatedHelpTemplate {

    @Override
    public int syntaxesPerPage() {
        return 5;
    }

    @Override
    public Component fullHeader(Command<?> command, int page, int maxPages) {
        return Messages.getMsg(
                "<dark_gray><bold><strikethrough>=================== <dark_green>"
                        + command.getName() + "'s help <pages_header> </dark_green>==================="
                , Placeholder.component("pages_header", pagesHeaderComponent(page, maxPages))
        );
    }


    private Component pagesHeaderComponent(int page, int maxPages) {
        return Messages.getMsg("<dark_gray>(</dark_gray> <green><page><gray>/</gray><max_pages> <dark_gray>)</dark_gray>",
                Placeholder.parsed("page", String.valueOf(page)),
                Placeholder.parsed("max_pages", String.valueOf(maxPages)));
    }

}
