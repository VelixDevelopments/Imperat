package dev.velix.imperat.context.internal;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
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
