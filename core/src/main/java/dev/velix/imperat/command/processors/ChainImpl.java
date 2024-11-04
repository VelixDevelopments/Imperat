package dev.velix.imperat.command.processors;

import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Queue;

record ChainImpl<S extends Source, P extends CommandProcessor>(
    Queue<P> processors) implements CommandProcessingChain<S, P> {
    @Override
    public @NotNull Queue<P> getProcessors() {
        return processors;
    }

    @Override
    public void reset() {
        processors.clear();
    }

    @Override
    public void add(P processor) {
        processors.add(processor);
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public @NotNull Iterator<P> iterator() {
        return processors.iterator();
    }
}
