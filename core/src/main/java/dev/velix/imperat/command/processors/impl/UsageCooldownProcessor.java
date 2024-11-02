package dev.velix.imperat.command.processors.impl;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.CooldownException;
import dev.velix.imperat.exception.ImperatException;

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
        var source = context.source();
        var handler = usage.getCooldownHandler();
        var cooldown = usage.getCooldown();

        if (handler.hasCooldown(source)) {
            assert cooldown != null;
            if (!imperat.config().getPermissionResolver().hasPermission(source, cooldown.permission())) {
                throw new CooldownException(
                    cooldown.toMillis(),
                    handler.getLastTimeExecuted(source).orElse(0L)
                );
            }
        }
        handler.registerExecutionMoment(source);
    }

}
