package dev.velix.imperat.misc;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.cooldown.CooldownHandler;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.CooldownException;
import dev.velix.imperat.exception.ImperatException;

public final class CustomCooldownProcessor<S extends Source> implements CommandPreProcessor<S> {
    /**
     * Processes context BEFORE the resolving operation.
     *
     * @param imperat the api
     * @param context the context
     * @param usage   The usage detected
     * @throws ImperatException the exception to throw if something happens
     */
    @Override
    public void process(Imperat<S> imperat, Context<S> context, CommandUsage<S> usage) throws ImperatException {
        CooldownHandler<S> cooldownHandler = usage.getCooldownHandler();
        S source = context.source();

        if (cooldownHandler.hasCooldown(source) && !imperat.config().getPermissionResolver().hasPermission(source, "yourpermission")) {
            //if there's a cooldown and the source doesn't have a specific permission, let's send him the cooldown message through the exception below
            throw new CooldownException(
                cooldownHandler.getUsageCooldown().orElseThrow().toMillis(),
                cooldownHandler.getLastTimeExecuted(source).orElse(0L)
            );
        }

    }
}
