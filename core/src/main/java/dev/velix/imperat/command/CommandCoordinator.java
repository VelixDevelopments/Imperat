package dev.velix.imperat.command;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.Source;
import dev.velix.imperat.context.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public interface CommandCoordinator<C> {

    void coordinate(
            @NotNull Source<C> source,
            @NotNull Context<C> context,
            @NotNull CommandExecution<C> execution
    );

    static <C> CommandCoordinator<C> sync() {
        return (source, context, execution) -> {
            try {
                execution.execute(source, context);
            } catch (Exception ex) {
                Imperat.handleException(context, CommandCoordinator.class, "sync-lambda", ex);
            }
        };
    }

    static <C> CommandCoordinator<C> async(final @Nullable ExecutorService service) {
        return ((source, context, execution) -> {
            ExecutorService executorService = service;
            if (executorService == null) {
                executorService = ForkJoinPool.commonPool();
            }
            CompletableFuture.runAsync(() -> {
                try {
                    execution.execute(source, context);
                } catch (Exception e) {
                    Imperat.handleException(context, CommandCoordinator.class, "async-lambda", e);
                }
            }, executorService);
        });
    }

    static <C> CommandCoordinator<C> async() {
        return async(null);
    }

}
