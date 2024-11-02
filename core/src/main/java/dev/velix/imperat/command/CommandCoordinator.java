package dev.velix.imperat.command;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public interface CommandCoordinator<S extends Source> {

    static <S extends Source> CommandCoordinator<S> sync() {
        return (api, source, context, execution) -> {
            try {
                execution.execute(source, context);
            } catch (Exception ex) {
                api.config().handleThrowable(ex, context, CommandCoordinator.class, "sync-lambda");
            }
        };
    }

    static <S extends Source> CommandCoordinator<S> async(final @Nullable ExecutorService service) {
        return ((api, source, context, execution) -> {
            ExecutorService executorService = service;
            if (executorService == null) {
                executorService = ForkJoinPool.commonPool();
            }
            CompletableFuture.runAsync(() -> {
                try {
                    execution.execute(source, context);
                } catch (Exception e) {
                    api.config().handleThrowable(e, context, CommandCoordinator.class, "async-lambda");
                }
            }, executorService);
        });
    }

    static <S extends Source> CommandCoordinator<S> async() {
        return async(null);
    }

    void coordinate(
        @NotNull Imperat<S> imperat,
        @NotNull S source,
        @NotNull ExecutionContext<S> context,
        @NotNull CommandExecution<S> execution
    );

}
