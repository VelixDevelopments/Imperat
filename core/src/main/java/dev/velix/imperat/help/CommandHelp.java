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
    ) throws ExecutionFailure {
        if (template == null) {
            throw new ExecutionFailure(CaptionKey.NO_HELP_AVAILABLE_CAPTION);
        }
        
        Command<S> command = (Command<S>) this.command;
        PaginatedText<CommandUsage<S>> text = new PaginatedText<>(template.syntaxesPerPage());
        
        for (CommandUsage<S> usage : command.getUsages()) {
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
        
        template.getUsagesDisplayer().display(command, source,
                template.getUsageFormatter(), textPage.asList());
        
        source.reply(template.getFooter(command));
    }
    
    private <S extends Source> void displayNormal(S source) throws ExecutionFailure {
        if (template == null) {
            throw new ExecutionFailure(CaptionKey.NO_HELP_AVAILABLE_CAPTION);
        }
        Command<S> command = (Command<S>) this.command;
        
        final int maxUsages = command.getUsages().size();
        if (maxUsages == 0) {
            throw new ExecutionFailure(CaptionKey.NO_HELP_AVAILABLE_CAPTION);
        }
        
        source.reply(template.getHeader(command));
        template.getUsagesDisplayer().display(command, source,
                template.getUsageFormatter(), new ArrayList<>(command.getUsages()));
        
        source.reply(template.getFooter(command));
    }
    
}
