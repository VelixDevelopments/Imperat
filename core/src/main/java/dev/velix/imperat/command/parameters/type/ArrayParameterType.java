package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class ArrayParameterType<S extends Source, E> extends BaseParameterType<S, E[]> {

    private final Function<Integer, Object[]> initializer;
    private final ParameterType<S, E> componentType;

    /**
     * Constructs a new BaseParameterType with the given TypeWrap.
     *
     * @param type The wrapping object that holds information about the type of the parameter.
     */
    public ArrayParameterType(TypeWrap<E[]> type, Function<Integer, Object[]> initializer, ParameterType<S, E> componentType) {
        super(type);
        this.initializer = initializer;
        this.componentType = componentType;
    }

    @Override @SuppressWarnings("unchecked")
    public E @Nullable [] resolve(@NotNull ExecutionContext<S> context, @NotNull CommandInputStream<S> stream, String input) throws ImperatException {

        String currentRaw = stream.currentRaw().orElse(null);
        if(currentRaw == null)
            return null;

        int arrayLength = stream.rawsLength()-stream.currentRawPosition();

        E[] array = (E[]) initializer.apply(arrayLength);

        int i = 0;
        while (stream.hasNextRaw()) {

            String raw = stream.currentRaw().orElse(null);
            if(raw == null)
                break;

            array[i] = componentType.resolve(context, stream, stream.readInput());

            stream.skipRaw();
            i++;
        }

        return array;
    }

    @Override @SuppressWarnings("unchecked")
    public E @NotNull [] fromString(Imperat<S> imperat, String input) throws ImperatException {
        String[] split = input.split(" ");
        E[] initializer = (E[]) this.initializer.apply(split.length);
        for (int i = 0; i < split.length; i++) {
            String raw = split[i];
            initializer[i] = componentType.fromString(imperat, raw);
        }
        return initializer;
    }
}
