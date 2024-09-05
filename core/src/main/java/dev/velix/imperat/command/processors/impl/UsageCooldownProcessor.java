package dev.velix.imperat.command.processors.impl;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.caption.CaptionKey;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exceptions.CommandException;
import dev.velix.imperat.exceptions.ExecutionFailure;

public final class UsageCooldownProcessor<S extends Source> implements CommandPreProcessor<S> {
    /**
     * Processes context BEFORE the resolving operation.
     *
     * @param imperat the api
     * @param command the command
     * @param context the context
     * @param usage   The usage detected
     * @throws CommandException the exception to throw if something happens
     */
    @Override
    public void process(
            Imperat<S> imperat,
            Command<S> command,
            Context<S> context,
            CommandUsage<S> usage
    ) throws CommandException {
        var source = context.getSource();
        if (usage.getCooldownHandler().hasCooldown(source)) {
            throw new ExecutionFailure(CaptionKey.COOLDOWN);
        }
        usage.getCooldownHandler().registerExecutionMoment(source);
    }
    
}
