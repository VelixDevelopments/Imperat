package dev.velix.context.internal;

import dev.velix.Imperat;
import dev.velix.command.Command;
import dev.velix.context.ArgumentQueue;
import dev.velix.context.Context;
import dev.velix.context.Source;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
class ContextImpl<S extends Source> implements Context<S> {
    
    protected final Imperat<S> dispatcher;
    
    private final Command<S> commandUsed;
    private final S source;
    private final ArgumentQueue raw;
    
    @Override
    public @NotNull Command<S> getCommandUsed() {
        return commandUsed;
    }
    
    @Override
    public @NotNull S getSource() {
        return source;
    }
    
    @Override
    public @NotNull ArgumentQueue getArguments() {
        return raw;
    }
    
    
}
