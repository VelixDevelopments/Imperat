package dev.velix.imperat.help;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("1.0.0")
@SuppressWarnings("unchecked")
public final class CommandHelp {
    
    private final Imperat<?> dispatcher;
    private final ExecutionContext<?> context;
    
    public CommandHelp(
            Imperat<?> dispatcher,
            ExecutionContext<?> context
    ) {
        this.dispatcher = dispatcher;
        this.context = context;
    }
    
    
    public <S extends Source> void display() {
        try {
            HelpProvider<S> provider = (HelpProvider<S>) dispatcher.getHelpProvider();
            if (provider != null)
                provider.provide((ExecutionContext<S>) context);
        } catch (Throwable ex) {
            ((Imperat<S>) dispatcher).handleThrowable(ex, (Context<S>) context, this.getClass(), "display(source, page)");
        }
    }
    
    
}
