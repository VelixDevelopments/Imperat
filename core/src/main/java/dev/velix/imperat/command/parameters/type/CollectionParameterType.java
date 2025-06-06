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

    /**
     * Constructs a new BaseParameterType with the given TypeWrap.
     *
     * @param type The wrapping object that holds information about the type of the parameter.
     */
    public CollectionParameterType(TypeWrap<C> type, Supplier<C> collectionSupplier, ParameterType<S, E> componentResolver) {
        super(type);
        this.collectionSupplier = collectionSupplier;
        this.componentResolver = componentResolver;
    }

    @Override
    public @Nullable C resolve(@NotNull ExecutionContext<S> context, @NotNull CommandInputStream<S> commandInputStream, String input) throws ImperatException {
        C newCollection = collectionSupplier.get();

        while (commandInputStream.hasNextRaw()) {

            String raw = commandInputStream.currentRaw().orElse(null);
            if(raw == null) break;
            //if(context.imperatConfig().getPar)
            E element = componentResolver.resolve(context, CommandInputStream.subStream(commandInputStream, raw), raw);
            newCollection.add(element);

            commandInputStream.skipRaw();
        }
        return newCollection;
    }

}
