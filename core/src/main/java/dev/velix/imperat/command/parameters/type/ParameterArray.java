package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class ParameterArray<S extends Source, E> extends BaseParameterType<S, E[]> {

    private final Function<Integer, Object[]> initializer;
    private final ParameterType<S, E> componentType;

    public ParameterArray(TypeWrap<E[]> type, Function<Integer, Object[]> initializer, ParameterType<S, E> componentType) {
        super(type.getType());
        this.initializer = initializer;
        this.componentType = componentType;
    }

    @Override @SuppressWarnings("unchecked")
    public E @Nullable [] resolve(@NotNull ExecutionContext<S> context, @NotNull CommandInputStream<S> stream, @NotNull String input) throws ImperatException {

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

            array[i] = componentType.resolve(context, CommandInputStream.subStream(stream, raw), raw);

            stream.skipRaw();
            i++;
        }

        return array;
    }

}
