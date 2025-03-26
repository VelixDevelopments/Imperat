package dev.velix.imperat.context.internal;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.ImperatConfig;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.*;

class ContextImpl<S extends Source> implements Context<S> {

    protected final Imperat<S> imperat;
    protected final ImperatConfig<S> dispatcher;

    private final Command<S> commandUsed;
    private final S source;
    private final ArgumentQueue raw;

    public ContextImpl(Imperat<S> imperat, Command<S> commandUsed, S source, ArgumentQueue raw) {
        this.imperat = imperat;
        this.dispatcher = imperat.config();
        this.commandUsed = commandUsed;
        this.source = source;
        this.raw = raw;
    }

    @Override
    public Imperat<S> imperat() {
        return imperat;
    }

    @Override
    public ImperatConfig<S> imperatConfig() {
        return dispatcher;
    }


    @Override
    public @NotNull Command<S> command() {
        return commandUsed;
    }

    @Override
    public @NotNull S source() {
        return source;
    }

    @Override
    public @NotNull ArgumentQueue arguments() {
        return raw;
    }


    public Command<S> getCommandUsed() {
        return this.commandUsed;
    }

    public S getSource() {
        return this.source;
    }

    public ArgumentQueue getRaw() {
        return this.raw;
    }

}
