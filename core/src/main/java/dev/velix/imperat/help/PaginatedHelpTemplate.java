package dev.velix.imperat.help;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.NoHelpException;
import dev.velix.imperat.exception.NoHelpPageException;
import dev.velix.imperat.util.text.PaginatedText;
import dev.velix.imperat.util.text.TextPage;
import org.jetbrains.annotations.*;

import java.util.Collection;

/**
 * Represents a help menu with pages
 */
@ApiStatus.AvailableSince("1.0.0")
public non-sealed abstract class PaginatedHelpTemplate<S extends Source> extends HelpTemplate<S> {

    protected final int syntaxesPerPage;
    protected PaginatedText<CommandUsage<S>> paginatedText;

    public PaginatedHelpTemplate(UsageFormatter formatter,
                                 int syntaxesPerPage) {
        super(formatter);
        this.syntaxesPerPage = syntaxesPerPage;
        paginatedText = new PaginatedText<>(syntaxesPerPage);
    }

    @Override
    public void display(ExecutionContext<S> context, S source, UsageFormatter formatter, Collection<? extends CommandUsage<S>> commandUsages) throws ImperatException {

        Integer page = context.getArgumentOr("page", 1);
        TextPage<CommandUsage<S>> textPage = paginatedText.getPage(page);
        if (textPage == null) {
            throw new NoHelpPageException();
        }
        int index = 0;
        for (var usage : textPage.asList()) {
            source.reply(formatter.format(context.command(), usage, index));
            index++;
        }
    }

    @Override
    public void provide(ExecutionContext<S> context, S source) throws ImperatException {
        Command<S> command = context.command();

        var commandUsages = command.usages();
        commandUsages.forEach(paginatedText::add);

        paginatedText.paginate();

        final int maxUsages = commandUsages.size();
        if (maxUsages == 0) {
            throw new NoHelpException();
        }
        int page = context.getArgumentOr("page", 1);
        displayHeaderHyphen(command, source, page, paginatedText.getMaxPages());
        display(context, source, formatter, commandUsages);
        displayFooterHyphen(command, source, page, paginatedText.getMaxPages());

        paginatedText.clear();

    }
}
