package dev.velix.imperat.coordinator;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.CommandSource;
import dev.velix.imperat.command.CommandExecution;
import dev.velix.imperat.context.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public interface CommandCoordinator<C> {

    void coordinate(
            @NotNull CommandSource<C> source,
            @NotNull Context<C> context,
            @NotNull CommandExecution<C> execution
    );

    static <C> CommandCoordinator<C> sync() {
        return (source, context, execution) -> {
            try {
                execution.execute(source, context);
            } catch (Exception ex) {
                CommandDispatcher.handleException(context, CommandCoordinator.class, "sync-lambda", ex);
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
                          CommandDispatcher.handleException(context, CommandCoordinator.class, "async-lambda", e);
                      }
				            }, executorService);
        });
    }

    static <C> CommandCoordinator<C> async() {
        return async(null);
    }

}
