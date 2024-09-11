package dev.velix.imperat.command.processors.impl;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.caption.CaptionKey;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ExecutionFailure;
import dev.velix.imperat.exception.ImperatException;

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
            throw new ExecutionFailure(CaptionKey.NO_PERMISSION);
        }
        
    }
}
