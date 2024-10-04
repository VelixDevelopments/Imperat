package dev.velix.imperat.context.internal;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

class ContextImpl<S extends Source> implements Context<S> {

    protected final Imperat<S> dispatcher;

    private final Command<S> commandUsed;
    private final S source;
    private final ArgumentQueue raw;

    public ContextImpl(Imperat<S> dispatcher, Command<S> commandUsed, S source, ArgumentQueue raw) {
        this.dispatcher = dispatcher;
        this.commandUsed = commandUsed;
        this.source = source;
        this.raw = raw;
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

    public Imperat<S> getDispatcher() {
        return this.dispatcher;
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

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ContextImpl<?> other)) return false;
        if (!other.canEqual(this)) return false;
        if (!Objects.equals(this.getDispatcher(), other.getDispatcher())) return false;
        if (!Objects.equals(this.getCommandUsed(), other.getCommandUsed())) return false;
        if (!Objects.equals(this.getSource(), other.getSource())) return false;
        return Objects.equals(this.getRaw(), other.getRaw());
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ContextImpl;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object dispatcher = this.getDispatcher();
        result = result * PRIME + (dispatcher == null ? 43 : dispatcher.hashCode());
        final Object commandUsed = this.getCommandUsed();
        result = result * PRIME + (commandUsed == null ? 43 : commandUsed.hashCode());
        final Object source = this.getSource();
        result = result * PRIME + (source == null ? 43 : source.hashCode());
        final Object raw = this.getRaw();
        result = result * PRIME + (raw == null ? 43 : raw.hashCode());
        return result;
    }

    public String toString() {
        return "ContextImpl(dispatcher=" + this.getDispatcher() + ", commandUsed=" + this.getCommandUsed() + ", source=" + this.getSource() + ", raw=" + this.getRaw() + ")";
    }
}
