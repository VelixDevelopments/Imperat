package dev.velix.imperat.help;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Source;

final class TemplatePagination<S extends Source> extends PaginatedHelpTemplate<S> {
    private final HelpTemplate<S> template;

    TemplatePagination(
        HelpTemplate<S> template,
        int syntaxesPerPage
    ) {
        super(template.formatter, syntaxesPerPage);
        this.template = template;
    }


    @Override
    public String getHeader(Command<S> command, int currentPage, int maxPages) {
        return template.getHeader(command, currentPage, maxPages);
    }

    @Override
    public String getFooter(Command<S> command, int currentPage, int maxPages) {
        return template.getFooter(command, currentPage, maxPages);
    }

    @Override
    public void displayHeaderHyphen(Command<S> command, Source source, int page) {
        source.reply(getHeader(command, page, paginatedText.getMaxPages()));
    }

    @Override
    public void displayFooterHyphen(Command<S> command, Source source, int page) {
        source.reply(getHeader(command, page, paginatedText.getMaxPages()));
    }
}