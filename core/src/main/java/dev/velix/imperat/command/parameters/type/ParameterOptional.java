package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;

public final class ParameterOptional<S extends Source, T> extends BaseParameterType<S, Optional<T>> {

    private final ParameterType<S, T> typeResolver;

    public ParameterOptional(TypeWrap<Optional<T>> typeWrap, ParameterType<S, T> typeResolver) {
        super(typeWrap.getType());
        this.typeResolver = typeResolver;
    }

    @Override
    public @NotNull Optional<T> resolve(
            @NotNull ExecutionContext<S> context,
            @NotNull CommandInputStream<S> inputStream,
            @NotNull String input
    ) throws ImperatException {
        return Optional.ofNullable(
                typeResolver.resolve(context,inputStream, input)
        );
    }
}
