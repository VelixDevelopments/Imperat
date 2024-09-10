package dev.velix.imperat.help;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.caption.CaptionKey;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ExecutionFailure;
import dev.velix.imperat.util.text.PaginatedText;
import dev.velix.imperat.util.text.TextPage;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;

@ApiStatus.AvailableSince("1.0.0")
public class CommandHelp<S extends Source> {

    private final Imperat<S> dispatcher;
    private final Context<S> context;
    private final Command<S> command;
    private final HelpTemplate template;

    public CommandHelp(
            Imperat<S> dispatcher,
            Command<S> command,
            Context<S> context
    ) {
        this.dispatcher = dispatcher;
        this.command = command;
        this.template = dispatcher.getHelpTemplate();
        this.context = context;
    }

    public void display(S source) {
        display(source, 1);
    }

    public void display(S source, int page) {
        try {
            if (template instanceof PaginatedHelpTemplate paginatedTemplate) {
                displayPaginated(source, paginatedTemplate, page);
            } else {
                displayNormal(source);
            }
        } catch (Throwable ex) {
            dispatcher.handleThrowable(ex, context, this.getClass(), "display(source, page)");
        }
    }

    private void displayPaginated(
            S source,
            PaginatedHelpTemplate template,
            int page
    ) throws ExecutionFailure {
        if (template == null) {
            throw new ExecutionFailure(CaptionKey.NO_HELP_AVAILABLE_CAPTION);
        }

        PaginatedText<CommandUsage<S>> text = new PaginatedText<>(template.syntaxesPerPage());

        for (var usage : command.getUsages()) {
            if (usage.isDefault()) continue;
            text.add(usage);
        }

        text.paginate();
        if (text.getMaxPages() == 0) {
            throw new ExecutionFailure(CaptionKey.NO_HELP_AVAILABLE_CAPTION);
        }

        TextPage<CommandUsage<S>> textPage = text.getPage(page);

        if (textPage == null) {
            throw new ExecutionFailure(CaptionKey.NO_HELP_PAGE_AVAILABLE_CAPTION);
        }
        source.reply(template.fullHeader(command, page, text.getMaxPages()));

        template.getUsagesDisplayer().display(dispatcher, command, source,
                template.getUsageFormatter(), textPage.asList());

        source.reply(template.getFooter(command));
    }

    private void displayNormal(S source) throws ExecutionFailure {
        if (template == null) {
            throw new ExecutionFailure(CaptionKey.NO_HELP_AVAILABLE_CAPTION);
        }

        final int maxUsages = command.getUsages().size();
        if (maxUsages == 0) {
            throw new ExecutionFailure(CaptionKey.NO_HELP_AVAILABLE_CAPTION);
        }

        source.reply(template.getHeader(command));
        template.getUsagesDisplayer().display(dispatcher, command, source,
                template.getUsageFormatter(), new ArrayList<>(command.getUsages()));

        source.reply(template.getFooter(command));
    }

}
