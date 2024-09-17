package dev.velix.command.processors.impl;

import dev.velix.Imperat;
import dev.velix.command.CommandUsage;
import dev.velix.command.processors.CommandPreProcessor;
import dev.velix.context.Context;
import dev.velix.context.Source;
import dev.velix.exception.CooldownException;
import dev.velix.exception.ImperatException;

public final class UsageCooldownProcessor<S extends Source> implements CommandPreProcessor<S> {
    /**
     * Processes context BEFORE the resolving operation.
     *
     * @param imperat the api
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
        if (usage.getCooldownHandler().hasCooldown(source)) {
            throw new CooldownException(
                    usage.getCooldownHandler().getUsageCooldown().orElseThrow().toMillis(),
                    usage.getCooldownHandler().getLastTimeExecuted(source).orElse(0L)
            );
        }
        usage.getCooldownHandler().registerExecutionMoment(source);
    }
    
}
