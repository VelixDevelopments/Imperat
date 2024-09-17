package dev.velix.help;

import dev.velix.Imperat;
import dev.velix.command.Command;
import dev.velix.command.CommandUsage;
import dev.velix.context.Context;
import dev.velix.context.Source;
import dev.velix.exception.ImperatException;
import dev.velix.exception.NoHelpException;
import dev.velix.exception.NoHelpPageException;
import dev.velix.util.text.PaginatedText;
import dev.velix.util.text.TextPage;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;

@ApiStatus.AvailableSince("1.0.0")
@SuppressWarnings("unchecked")
public final class CommandHelp {
    
    private final Imperat<?> dispatcher;
    private final Context<?> context;
    private final Command<?> command;
    private final HelpTemplate template;
    
    public CommandHelp(
            Imperat<?> dispatcher,
            Command<?> command,
            Context<?> context
    ) {
        this.dispatcher = dispatcher;
        this.command = command;
        this.template = dispatcher.getHelpTemplate();
        this.context = context;
    }
    
    public <S extends Source> void display(S source) {
        display(source, 1);
    }
    
    public <S extends Source> void display(S source, int page) {
        try {
            if (template instanceof PaginatedHelpTemplate paginatedTemplate) {
                displayPaginated(source, paginatedTemplate, page);
            } else {
                displayNormal(source);
            }
        } catch (Throwable ex) {
            ((Imperat<S>) dispatcher).handleThrowable(ex, (Context<S>) context, this.getClass(), "display(source, page)");
        }
    }
    
    private <S extends Source> void displayPaginated(
            S source,
            PaginatedHelpTemplate template,
            int page
    ) throws ImperatException {
        if (template == null) {
            throw new NoHelpException();
        }
        
        Command<S> command = (Command<S>) this.command;
        PaginatedText<CommandUsage<S>> text = new PaginatedText<>(template.syntaxesPerPage());
        
        for (CommandUsage<S> usage : command.getUsages()) {
            if (usage.isDefault()) continue;
            text.add(usage);
        }
        
        text.paginate();
        if (text.getMaxPages() == 0) {
            throw new NoHelpPageException();
        }
        
        TextPage<CommandUsage<S>> textPage = text.getPage(page);
        
        if (textPage == null) {
            throw new NoHelpPageException();
        }
        source.reply(template.fullHeader(command, page, text.getMaxPages()));
        
        template.getUsagesDisplayer().display(command, source,
                template.getUsageFormatter(), textPage.asList());
        
        source.reply(template.getFooter(command));
    }
    
    private <S extends Source> void displayNormal(S source) throws ImperatException {
        if (template == null) {
            throw new NoHelpException();
        }
        Command<S> command = (Command<S>) this.command;
        
        final int maxUsages = command.getUsages().size();
        if (maxUsages == 0) {
            throw new NoHelpException();
        }
        
        source.reply(template.getHeader(command));
        template.getUsagesDisplayer().display(command, source,
                template.getUsageFormatter(), new ArrayList<>(command.getUsages()));
        
        source.reply(template.getFooter(command));
    }
    
}
