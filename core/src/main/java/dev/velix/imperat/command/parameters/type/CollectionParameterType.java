package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Supplier;

public class CollectionParameterType<S extends Source, E, C extends Collection<E>> extends BaseParameterType<S, C> {

    private final Supplier<C> collectionSupplier;
    private final ParameterType<S, E> componentResolver;

    public CollectionParameterType(TypeWrap<C> type, Supplier<C> collectionSupplier, ParameterType<S, E> componentResolver) {
        super(type.getType());
        this.collectionSupplier = collectionSupplier;
        this.componentResolver = componentResolver;
    }

    @Override
    public @Nullable C resolve(@NotNull ExecutionContext<S> context, @NotNull CommandInputStream<S> commandInputStream, @NotNull String input) throws ImperatException {
        C newCollection = collectionSupplier.get();

        while (commandInputStream.hasNextRaw()) {

            String raw = commandInputStream.currentRaw().orElse(null);
            if(raw == null) break;

            E element = componentResolver.resolve(context, CommandInputStream.subStream(commandInputStream, raw), raw);
            newCollection.add(element);

            commandInputStream.skipRaw();
        }
        return newCollection;
    }

}
