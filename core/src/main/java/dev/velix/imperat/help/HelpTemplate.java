package dev.velix.imperat.help;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.NoHelpException;
import org.jetbrains.annotations.*;

/**
 * Represents a template for holding information that the help-writer will use
 * to display the help menu of a single command to the command sender
 */
@ApiStatus.AvailableSince("1.0.0")
public sealed abstract class HelpTemplate<S extends Source> implements HelpProvider<S> permits HelpTemplateImpl, PaginatedHelpTemplate {

    protected final UsageFormatter formatter;

    public HelpTemplate(UsageFormatter formatter) {
        this.formatter = formatter;
    }

    /**
     * @param command the command
     * @return the header
     */
    public abstract String getHeader(Command<S> command, int currentPage, int maxPages);

    /**
     * @param command the command
     * @return the footer
     */
    public abstract String getFooter(Command<S> command, int currentPage, int maxPages);


    @Override
    public void provide(ExecutionContext<S> context, Source source) throws ImperatException {
        Command<S> command = context.command();

        final int maxUsages = command.usages().size();
        if (maxUsages == 0) {
            throw new NoHelpException();
        }
        int page = context.getArgumentOr("page", 1);
        displayHeaderHyphen(command, source, page);
        display(context, source, formatter, command.usages());
        displayFooterHyphen(command, source, page);
    }

    public abstract void displayHeaderHyphen(Command<S> command, Source source, int page);

    public abstract void displayFooterHyphen(Command<S> command, Source source, int page);

}
