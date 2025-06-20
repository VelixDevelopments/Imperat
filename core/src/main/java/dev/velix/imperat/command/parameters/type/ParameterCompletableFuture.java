package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

public final class ParameterCompletableFuture<S extends Source, T> extends BaseParameterType<S, CompletableFuture<T>> {

    private final ParameterType<S, T> typeResolver;
    public ParameterCompletableFuture(TypeWrap<CompletableFuture<T>> typeWrap, ParameterType<S, T> typeResolver) {
        super(typeWrap.getType());
        this.typeResolver = typeResolver;
    }

    @Override
    public @NotNull CompletableFuture< @Nullable T> resolve(
            @NotNull ExecutionContext<S> context,
            @NotNull CommandInputStream<S> inputStream,
            @NotNull String input) throws ImperatException {

        if(typeResolver == null) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("No type parameter for type '" + type.getTypeName() + "'")
            );
        }

        return CompletableFuture.supplyAsync(()-> {
            try {
                return typeResolver.resolve(context,inputStream, input);
            } catch (ImperatException e) {
                context.imperatConfig()
                        .handleExecutionThrowable(e,context, ParameterCompletableFuture.class, "resolve");
                return null;
            }
        });
    }
}
