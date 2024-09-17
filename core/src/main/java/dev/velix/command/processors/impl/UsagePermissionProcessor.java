package dev.velix.command.processors.impl;

import dev.velix.Imperat;
import dev.velix.command.CommandUsage;
import dev.velix.command.processors.CommandPreProcessor;
import dev.velix.context.Context;
import dev.velix.context.Source;
import dev.velix.exception.ImperatException;
import dev.velix.exception.PermissionDeniedException;

public final class UsagePermissionProcessor<S extends Source> implements CommandPreProcessor<S> {
    /**
     * Processes context BEFORE the resolving operation.
     *
     * @param context the context
     * @param usage   The usage detected
     * @throws ImperatException the exception to throw if something happens
     */
    @Override
    public void process(
            Imperat<S> imperat,
            Context<S> context,
            CommandUsage<S> usage
    ) throws ImperatException {
        var source = context.getSource();
        
        if (!imperat.getPermissionResolver().hasUsagePermission(source, usage)) {
            throw new PermissionDeniedException();
        }
        
    }
}
