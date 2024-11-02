package dev.velix.imperat.command.cooldown;

import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Source;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class DefaultCooldownHandler<S extends Source> implements CooldownHandler<S> {

    private final Map<S, Long> lastTimeExecuted = new HashMap<>();
    private final CommandUsage<S> usage;

    DefaultCooldownHandler(CommandUsage<S> usage) {
        this.usage = usage;
    }


    /**
     * Sets the last time of execution to this
     * current moment using {@link System#currentTimeMillis()}
     *
     * @param source the command sender executing the {@link CommandUsage}
     */
    @Override
    public void registerExecutionMoment(S source) {
        lastTimeExecuted.put(source, System.currentTimeMillis());
    }

    /**
     * The required of a usage
     *
     * @return the container of usage's cooldown, the container may be empty
     */
    @Override
    public Optional<UsageCooldown> getUsageCooldown() {
        return Optional.ofNullable(usage.getCooldown());
    }

    /**
     * Unregisters the user's cached cooldown
     * when it's expired!
     *
     * @param source the command-sender
     */
    @Override
    public void removeCooldown(S source) {
        lastTimeExecuted.remove(source);
    }

    /**
     * Fetches the last time the command source
     * executed a specific command usage
     *
     * @param source the command sender
     * @return the last time the sender executed {@link CommandUsage}
     */
    @Override
    public Optional<Long> getLastTimeExecuted(S source) {
        return Optional.ofNullable(lastTimeExecuted.get(source));
    }
}
