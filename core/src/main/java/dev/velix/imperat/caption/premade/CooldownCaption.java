package dev.velix.imperat.caption.premade;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.caption.CaptionKey;
import dev.velix.imperat.caption.Messages;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.cooldown.CooldownHandler;
import dev.velix.imperat.command.cooldown.UsageCooldown;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public final class CooldownCaption<S extends Source> implements Caption<S> {

    /**
     * @return the key
     */
    @Override
    public @NotNull CaptionKey getKey() {
        return CaptionKey.COOLDOWN;
    }

    /**
     * @param dispatcher the dispatcher
     * @param context    the context
     * @param exception  the exception may be null if no exception provided
     * @return The message in the form of a component
     */
    @Override
    public @NotNull String getMessage(
            @NotNull Imperat<S> dispatcher,
            @NotNull Context<S> context,
            @Nullable Exception exception
    ) {
        if (!(context instanceof ResolvedContext<S> resolvedContext)) {
            return "";
        }
        var usage = resolvedContext.getDetectedUsage();
        if (usage == null || usage.getCooldown() == null) {
            return "";
        }
        return Messages.COOL_DOWN_WAIT.replace("<time>", formatTime(context.getSource(), usage));
    }

    private String formatTime(S source, CommandUsage<S> usage) {
        return formatTime(source, usage.getCooldown(), usage.getCooldownHandler());
    }

    private String formatTime(S source,
                              UsageCooldown cooldown,
                              CooldownHandler<S> cooldownHandler) {
        return formatTime(cooldown, cooldownHandler.getLastTimeExecuted(source).orElse(-1L));
    }

    private String formatTime(UsageCooldown cooldown, long lastTimeExecuted) {
        long timePassed = System.currentTimeMillis() - lastTimeExecuted;
        long remaining = cooldown.toMillis() - timePassed;
        return TimeUnit.MILLISECONDS.toSeconds(remaining) + " second(s) ";
    }
}
