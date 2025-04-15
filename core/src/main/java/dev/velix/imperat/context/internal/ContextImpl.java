package dev.velix.imperat.context.internal;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.ImperatConfig;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;

class ContextImpl<S extends Source> implements Context<S> {

    protected final Imperat<S> imperat;
    protected final ImperatConfig<S> imperatConfig;

    private final Command<S> commandUsed;
    private final S source;
    private final String label;
    private final ArgumentQueue raw;

    public ContextImpl(Imperat<S> imperat, Command<S> commandUsed, S source, String label, ArgumentQueue raw) {
        this.imperat = imperat;
        this.imperatConfig = imperat.config();
        this.commandUsed = commandUsed;
        this.source = source;
        this.label = label;
        this.raw = raw;
    }

    @Override
    public Imperat<S> imperat() {
        return imperat;
    }

    @Override
    public ImperatConfig<S> imperatConfig() {
        return imperatConfig;
    }


    @Override
    public @NotNull Command<S> command() {
        return commandUsed;
    }

    @Override
    public @NotNull S source() {
        return source;
    }

    /**
     * @return the root command entered by the {@link Source}
     */
    @Override
    public @NotNull String label() {
        return label;
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
